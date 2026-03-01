/**
 * Annotation Editor JavaScript
 *
 * Visual annotation tool for marking up content during workflow review.
 * Supports pins, rectangles, highlights, arrows, and freeform drawing.
 */
(function(document, $) {
    "use strict";

    var EDITOR_SELECTOR = ".workflow-annotation-editor";

    /**
     * AnnotationEditor class
     */
    function AnnotationEditor(element) {
        this.$el = $(element);
        this.contentPath = this.$el.data("content-path");
        this.workflowId = this.$el.data("workflow-id");

        this.currentTool = "pin";
        this.currentColor = "#E53935";
        this.annotations = [];
        this.undoStack = [];
        this.redoStack = [];
        this.isDrawing = false;
        this.currentPath = [];

        this.init();
    }

    AnnotationEditor.prototype = {
        init: function() {
            this.$canvas = this.$el.find(".annotation-layer");
            this.$drawingLayer = this.$canvas.find(".drawing-layer");
            this.$contentLayer = this.$el.find(".content-layer");
            this.$popover = this.$el.find(".annotation-popover");

            this.loadExistingAnnotations();
            this.bindEvents();
        },

        bindEvents: function() {
            var self = this;

            // Tool selection
            this.$el.on("click", ".tool-button[data-tool]", function() {
                self.selectTool($(this).data("tool"));
            });

            // Color selection
            this.$el.on("click", ".color-button", function() {
                self.selectColor($(this).data("color"));
            });

            // Undo/Redo
            this.$el.on("click", "[data-action='undo']", function() { self.undo(); });
            this.$el.on("click", "[data-action='redo']", function() { self.redo(); });
            this.$el.on("click", "[data-action='clear-all']", function() { self.clearAll(); });

            // Canvas interactions
            this.$canvas.on("mousedown", function(e) { self.handleMouseDown(e); });
            this.$canvas.on("mousemove", function(e) { self.handleMouseMove(e); });
            this.$canvas.on("mouseup", function(e) { self.handleMouseUp(e); });

            // Annotation clicks
            this.$canvas.on("click", ".annotation", function(e) {
                e.stopPropagation();
                self.showAnnotationPopover($(this).data("id"));
            });

            // Popover actions
            this.$popover.on("click", "[data-action='save-annotation']", function() {
                self.saveCurrentAnnotation();
            });
            this.$popover.on("click", "[data-action='delete-annotation']", function() {
                self.deleteCurrentAnnotation();
            });
            this.$popover.on("click", "[data-action='close-popover']", function() {
                self.$popover.hide();
            });

            // List actions
            this.$el.on("click", ".annotation-item [data-action='goto']", function() {
                var id = $(this).closest(".annotation-item").data("annotation-id");
                self.goToAnnotation(id);
            });
            this.$el.on("click", ".annotation-item [data-action='delete']", function() {
                var id = $(this).closest(".annotation-item").data("annotation-id");
                self.deleteAnnotation(id);
            });
            this.$el.on("click", ".annotation-item [data-action='resolve']", function() {
                var id = $(this).closest(".annotation-item").data("annotation-id");
                self.resolveAnnotation(id);
            });
        },

        selectTool: function(tool) {
            this.currentTool = tool;
            this.$el.find(".tool-button[data-tool]").removeClass("active");
            this.$el.find(".tool-button[data-tool='" + tool + "']").addClass("active");

            // Update cursor
            this.$canvas.css("cursor", this.getCursorForTool(tool));
        },

        getCursorForTool: function(tool) {
            switch(tool) {
                case "pin": return "crosshair";
                case "rectangle": return "crosshair";
                case "draw": return "url(/apps/demo-workflow/clientlibs/workflow-review/pencil.cur), crosshair";
                case "highlight": return "crosshair";
                case "arrow": return "crosshair";
                default: return "default";
            }
        },

        selectColor: function(color) {
            this.currentColor = color;
            this.$el.find(".color-button").removeClass("active");
            this.$el.find(".color-button[data-color='" + color + "']").addClass("active");
        },

        handleMouseDown: function(e) {
            var pos = this.getMousePosition(e);

            if (this.currentTool === "pin") {
                this.createPinAnnotation(pos);
            } else {
                this.isDrawing = true;
                this.startPos = pos;
                this.currentPath = [pos];
            }
        },

        handleMouseMove: function(e) {
            if (!this.isDrawing) return;

            var pos = this.getMousePosition(e);
            this.currentPath.push(pos);

            if (this.currentTool === "draw") {
                this.updateDrawingPreview();
            } else {
                this.updateShapePreview(pos);
            }
        },

        handleMouseUp: function(e) {
            if (!this.isDrawing) return;

            var pos = this.getMousePosition(e);
            this.isDrawing = false;

            switch(this.currentTool) {
                case "rectangle":
                case "highlight":
                    this.createRectAnnotation(pos);
                    break;
                case "arrow":
                    this.createArrowAnnotation(pos);
                    break;
                case "draw":
                    this.createDrawAnnotation();
                    break;
            }

            this.$drawingLayer.empty();
        },

        getMousePosition: function(e) {
            var rect = this.$canvas[0].getBoundingClientRect();
            return {
                x: e.clientX - rect.left,
                y: e.clientY - rect.top
            };
        },

        createPinAnnotation: function(pos) {
            var annotation = {
                id: this.generateId(),
                type: "pin",
                x: pos.x,
                y: pos.y,
                color: this.currentColor,
                comment: "",
                author: this.getCurrentUser(),
                date: new Date().toISOString(),
                resolved: false
            };

            this.addAnnotation(annotation);
            this.showAnnotationPopover(annotation.id);
        },

        createRectAnnotation: function(endPos) {
            var annotation = {
                id: this.generateId(),
                type: this.currentTool,
                x: Math.min(this.startPos.x, endPos.x),
                y: Math.min(this.startPos.y, endPos.y),
                width: Math.abs(endPos.x - this.startPos.x),
                height: Math.abs(endPos.y - this.startPos.y),
                color: this.currentColor,
                comment: "",
                author: this.getCurrentUser(),
                date: new Date().toISOString(),
                resolved: false
            };

            if (annotation.width > 5 && annotation.height > 5) {
                this.addAnnotation(annotation);
                this.showAnnotationPopover(annotation.id);
            }
        },

        createArrowAnnotation: function(endPos) {
            var annotation = {
                id: this.generateId(),
                type: "arrow",
                x1: this.startPos.x,
                y1: this.startPos.y,
                x2: endPos.x,
                y2: endPos.y,
                color: this.currentColor,
                comment: "",
                author: this.getCurrentUser(),
                date: new Date().toISOString(),
                resolved: false
            };

            this.addAnnotation(annotation);
            this.showAnnotationPopover(annotation.id);
        },

        createDrawAnnotation: function() {
            if (this.currentPath.length < 2) return;

            var pathData = "M " + this.currentPath[0].x + " " + this.currentPath[0].y;
            for (var i = 1; i < this.currentPath.length; i++) {
                pathData += " L " + this.currentPath[i].x + " " + this.currentPath[i].y;
            }

            var annotation = {
                id: this.generateId(),
                type: "draw",
                path: pathData,
                color: this.currentColor,
                comment: "",
                author: this.getCurrentUser(),
                date: new Date().toISOString(),
                resolved: false
            };

            this.addAnnotation(annotation);
            this.showAnnotationPopover(annotation.id);
        },

        addAnnotation: function(annotation) {
            annotation.index = this.annotations.length + 1;
            this.annotations.push(annotation);
            this.undoStack.push({ action: "add", annotation: annotation });
            this.redoStack = [];

            this.renderAnnotation(annotation);
            this.updateAnnotationsList();
            this.saveAnnotations();
        },

        renderAnnotation: function(annotation) {
            var $group = this.$canvas.find(".annotations-group");
            var svg = this.createSvgElement(annotation);
            $group.append(svg);
        },

        createSvgElement: function(annotation) {
            var ns = "http://www.w3.org/2000/svg";
            var g = document.createElementNS(ns, "g");
            g.setAttribute("class", "annotation " + annotation.type);
            g.setAttribute("data-id", annotation.id);

            switch(annotation.type) {
                case "pin":
                    g.setAttribute("transform", "translate(" + annotation.x + "," + annotation.y + ")");
                    var circle = document.createElementNS(ns, "circle");
                    circle.setAttribute("r", "12");
                    circle.setAttribute("fill", annotation.color);
                    var text = document.createElementNS(ns, "text");
                    text.setAttribute("y", "5");
                    text.setAttribute("text-anchor", "middle");
                    text.setAttribute("fill", "white");
                    text.setAttribute("font-size", "10");
                    text.textContent = annotation.index;
                    g.appendChild(circle);
                    g.appendChild(text);
                    break;

                case "rectangle":
                    var rect = document.createElementNS(ns, "rect");
                    rect.setAttribute("x", annotation.x);
                    rect.setAttribute("y", annotation.y);
                    rect.setAttribute("width", annotation.width);
                    rect.setAttribute("height", annotation.height);
                    rect.setAttribute("stroke", annotation.color);
                    rect.setAttribute("stroke-width", "2");
                    rect.setAttribute("fill", "none");
                    g.appendChild(rect);
                    break;

                case "highlight":
                    var rect = document.createElementNS(ns, "rect");
                    rect.setAttribute("x", annotation.x);
                    rect.setAttribute("y", annotation.y);
                    rect.setAttribute("width", annotation.width);
                    rect.setAttribute("height", annotation.height);
                    rect.setAttribute("fill", annotation.color);
                    rect.setAttribute("opacity", "0.3");
                    g.appendChild(rect);
                    break;

                case "arrow":
                    var line = document.createElementNS(ns, "line");
                    line.setAttribute("x1", annotation.x1);
                    line.setAttribute("y1", annotation.y1);
                    line.setAttribute("x2", annotation.x2);
                    line.setAttribute("y2", annotation.y2);
                    line.setAttribute("stroke", annotation.color);
                    line.setAttribute("stroke-width", "2");
                    line.setAttribute("marker-end", "url(#arrowhead)");
                    g.appendChild(line);
                    break;

                case "draw":
                    var path = document.createElementNS(ns, "path");
                    path.setAttribute("d", annotation.path);
                    path.setAttribute("stroke", annotation.color);
                    path.setAttribute("stroke-width", "2");
                    path.setAttribute("fill", "none");
                    g.appendChild(path);
                    break;
            }

            return g;
        },

        showAnnotationPopover: function(id) {
            var annotation = this.getAnnotationById(id);
            if (!annotation) return;

            this.currentAnnotationId = id;

            this.$popover.find(".annotation-number").text("#" + annotation.index);
            this.$popover.find(".annotation-comment").val(annotation.comment || "");
            this.$popover.find(".annotation-author").text(annotation.author + " • " + this.formatDate(annotation.date));

            // Position popover near annotation
            var pos = this.getAnnotationPosition(annotation);
            this.$popover.css({
                left: pos.x + 20,
                top: pos.y,
                display: "block"
            });
        },

        getAnnotationPosition: function(annotation) {
            switch(annotation.type) {
                case "pin": return { x: annotation.x, y: annotation.y };
                case "rectangle":
                case "highlight": return { x: annotation.x + annotation.width, y: annotation.y };
                case "arrow": return { x: annotation.x2, y: annotation.y2 };
                case "draw": return { x: this.currentPath[0].x, y: this.currentPath[0].y };
                default: return { x: 0, y: 0 };
            }
        },

        saveCurrentAnnotation: function() {
            var comment = this.$popover.find(".annotation-comment").val();
            var annotation = this.getAnnotationById(this.currentAnnotationId);

            if (annotation) {
                annotation.comment = comment;
                this.updateAnnotationsList();
                this.saveAnnotations();
            }

            this.$popover.hide();
        },

        deleteCurrentAnnotation: function() {
            this.deleteAnnotation(this.currentAnnotationId);
            this.$popover.hide();
        },

        deleteAnnotation: function(id) {
            var index = this.annotations.findIndex(function(a) { return a.id === id; });
            if (index > -1) {
                var annotation = this.annotations.splice(index, 1)[0];
                this.undoStack.push({ action: "delete", annotation: annotation, index: index });
                this.$canvas.find(".annotation[data-id='" + id + "']").remove();
                this.updateAnnotationsList();
                this.saveAnnotations();
            }
        },

        resolveAnnotation: function(id) {
            var annotation = this.getAnnotationById(id);
            if (annotation) {
                annotation.resolved = true;
                this.updateAnnotationsList();
                this.saveAnnotations();
            }
        },

        goToAnnotation: function(id) {
            var $annotation = this.$canvas.find(".annotation[data-id='" + id + "']");
            if ($annotation.length) {
                // Flash animation
                $annotation.addClass("highlight-flash");
                setTimeout(function() {
                    $annotation.removeClass("highlight-flash");
                }, 1000);
            }
        },

        undo: function() {
            if (this.undoStack.length === 0) return;

            var action = this.undoStack.pop();
            this.redoStack.push(action);

            if (action.action === "add") {
                this.deleteAnnotation(action.annotation.id);
            } else if (action.action === "delete") {
                this.annotations.splice(action.index, 0, action.annotation);
                this.renderAnnotation(action.annotation);
                this.updateAnnotationsList();
            }
        },

        redo: function() {
            if (this.redoStack.length === 0) return;

            var action = this.redoStack.pop();
            this.undoStack.push(action);

            if (action.action === "add") {
                this.annotations.push(action.annotation);
                this.renderAnnotation(action.annotation);
                this.updateAnnotationsList();
            } else if (action.action === "delete") {
                this.deleteAnnotation(action.annotation.id);
            }
        },

        clearAll: function() {
            if (!confirm("Are you sure you want to clear all annotations?")) return;

            this.undoStack.push({ action: "clear", annotations: this.annotations.slice() });
            this.annotations = [];
            this.$canvas.find(".annotations-group").empty();
            this.updateAnnotationsList();
            this.saveAnnotations();
        },

        getAnnotationById: function(id) {
            return this.annotations.find(function(a) { return a.id === id; });
        },

        updateAnnotationsList: function() {
            // Trigger list update - would re-render in production
            this.$el.find(".annotations-list-panel .panel-header h4").text("Annotations (" + this.annotations.length + ")");
        },

        loadExistingAnnotations: function() {
            // In production, load from server via AJAX
            var self = this;
            $.ajax({
                url: "/bin/workflow/annotations",
                data: { workflowId: this.workflowId, contentPath: this.contentPath },
                success: function(data) {
                    if (data && data.annotations) {
                        self.annotations = data.annotations;
                        self.annotations.forEach(function(a) {
                            self.renderAnnotation(a);
                        });
                        self.updateAnnotationsList();
                    }
                }
            });
        },

        saveAnnotations: function() {
            // In production, save to server via AJAX
            $.ajax({
                url: "/bin/workflow/annotations",
                method: "POST",
                data: JSON.stringify({
                    workflowId: this.workflowId,
                    contentPath: this.contentPath,
                    annotations: this.annotations
                }),
                contentType: "application/json"
            });
        },

        generateId: function() {
            return "ann-" + Date.now() + "-" + Math.random().toString(36).substr(2, 9);
        },

        getCurrentUser: function() {
            return Granite.author ? Granite.author.user.name : "Unknown";
        },

        formatDate: function(isoDate) {
            var date = new Date(isoDate);
            return date.toLocaleDateString() + " " + date.toLocaleTimeString();
        },

        updateShapePreview: function(pos) {
            this.$drawingLayer.empty();
            var ns = "http://www.w3.org/2000/svg";
            var rect = document.createElementNS(ns, "rect");
            rect.setAttribute("x", Math.min(this.startPos.x, pos.x));
            rect.setAttribute("y", Math.min(this.startPos.y, pos.y));
            rect.setAttribute("width", Math.abs(pos.x - this.startPos.x));
            rect.setAttribute("height", Math.abs(pos.y - this.startPos.y));
            rect.setAttribute("stroke", this.currentColor);
            rect.setAttribute("stroke-width", "2");
            rect.setAttribute("stroke-dasharray", "5,5");
            rect.setAttribute("fill", this.currentTool === "highlight" ? this.currentColor : "none");
            rect.setAttribute("opacity", this.currentTool === "highlight" ? "0.3" : "1");
            this.$drawingLayer.append(rect);
        },

        updateDrawingPreview: function() {
            var pathData = "M " + this.currentPath[0].x + " " + this.currentPath[0].y;
            for (var i = 1; i < this.currentPath.length; i++) {
                pathData += " L " + this.currentPath[i].x + " " + this.currentPath[i].y;
            }

            this.$drawingLayer.empty();
            var ns = "http://www.w3.org/2000/svg";
            var path = document.createElementNS(ns, "path");
            path.setAttribute("d", pathData);
            path.setAttribute("stroke", this.currentColor);
            path.setAttribute("stroke-width", "2");
            path.setAttribute("fill", "none");
            this.$drawingLayer.append(path);
        }
    };

    // Initialize
    $(document).on("foundation-contentloaded", function() {
        $(EDITOR_SELECTOR).each(function() {
            new AnnotationEditor(this);
        });
    });

})(document, Granite.$);
