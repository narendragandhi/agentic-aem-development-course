# Common Issues and Solutions
# Instructor Guide

---

## Technical Issues

### Environment Setup Issues

#### Java Version Mismatch

**Symptom:** Build fails with "Source option 11 is no longer supported"

**Solution:**
```bash
# Check Java version
java -version

# Set correct JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Verify Maven uses correct Java
mvn -version
```

**Prevention:** Verify Java 11+ before course starts

---

#### Maven Build Failures

**Symptom:** `mvn clean install` fails with dependency errors

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Update settings
mvn dependency:resolve

# Retry build
mvn clean install
```

---

#### Docker/ClamAV Issues

**Symptom:** ClamAV container not responding

**Solution:**
```bash
# Restart Docker Desktop
# Or on Linux:
sudo systemctl restart docker

# Wait for ClamAV to initialize (2-3 minutes for virus DB)
docker logs clamav

# Test connectivity
docker exec clamav clamdscan --version
```

**Alternative:** Use MOCK mode for development:
```bash
# In OSGi config, set:
scanEngine="MOCK"
```

---

### AEM Issues

#### AEM Won't Start

**Symptom:** Quickstart doesn't start, no error

**Solutions:**
1. Check Java version (must be 11+)
2. Check port 4502 is available
3. Check sufficient disk space
4. Remove lock file: `rm -f crx-quickstart/.lock`

---

#### Bundle Won't Activate

**Symptom:** Bundle in "Installed" state, not "Active"

**Solution:**
```bash
# Check OSGi console: http://localhost:4502/system/console/bundles
# Look for unsatisfied dependencies

# Common fixes:
# 1. Missing import package - add to pom.xml
# 2. Unsatisfied service reference - check @Reference
# 3. Missing dependency - add to pom.xml
```

---

#### Workflow Not Triggering

**Symptom:** Upload doesn't start workflow

**Solutions:**
1. Verify workflow launcher exists in `/etc/workflow/launcher`
2. Check glob pattern matches upload path
3. Ensure launcher is enabled
4. Check run mode matches (author vs publish)

---

### Development Issues

#### OSGi Service Not Injecting

**Symptom:** NullPointerException when using @Reference

**Common Causes:**
1. Missing `@Component` annotation on service
2. Wrong service interface
3. Service not activated

**Solution:**
```java
@Component(service = MyService.class)
public class MyServiceImpl implements MyService {
    @Reference
    private DependencyService dependency;
}
```

---

#### Tests Failing

**Symptom:** Unit tests fail with NullPointerException

**Solution:**
```java
@BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
}
```

Or with AEM Mock:
```java
@ExtendWith(AemContextExtension.class)
class MyTest {
    @Inject
    private MyService service;
}
```

---

## Learning Issues

### Students Over-Relying on AI

**Symptom:** Student accepts all AI suggestions without review

**Intervention:**
1. Require manual code review step in each lab
2. Ask students to explain AI-generated code
3. Add code explanation requirement to assessments

**Prevention:** Emphasize critical thinking early

---

### Students Rejecting AI Suggestions

**Symptom:** Student ignores AI, does everything manually

**Intervention:**
1. Show successful AI examples
2. Demonstrate time savings
3. Compare manual vs AI approach

---

### Context Confusion

**Symptom:** Students forget BMAD-BEAD-GasTown relationship

**Solution:**
- Provide quick reference card
- Review diagram at start of each session
- Use the integration diagram regularly

---

### Falling Behind

**Symptom:** Student unable to complete lab in time

**Solutions:**
1. Provide completed code checkpoints
2. Allow pair programming
3. Offer extended time for complex labs

---

### PRD Writing Issues

**Symptom:** Vague acceptance criteria

**Before:**
```
System should be fast
```

**After:**
```
Scan completes within 60 seconds for files up to 100MB
```

**Solution:** Provide examples, use checklist

---

### TDD Confusion

**Symptom:** Student writes code before tests

**Solution:**
1. Emphasize RED phase (tests fail first)
2. Show the workflow diagram
3. Require spec files before implementation files

---

## Assessment Issues

### Quiz Questions

**Common Confusion Points:**
- BMAD phase numbers (00-06 vs 01-07)
- BEAD hierarchy order
- AEM workflow storage location (/conf vs /etc)

**Solution:** Create quick reference sheet

---

### Lab Submission Issues

**Common Issues:**
1. Missing files
2. Code doesn't compile
3. Tests not passing

**Solution:** Require pre-submission verification:
```bash
mvn clean verify
# Must show BUILD SUCCESS
```

---

## Debugging Tips

### For Students

1. **Read error messages carefully**
2. **Check logs first** - AEM error.log, Maven console
3. **Isolate the problem** - comment out code to find issue
4. **Use IDE debugger** - set breakpoints
5. **Search before asking** - Google error messages

### For Instructors

1. **Reproduce the issue** - see it firsthand
2. **Check environment** - verify versions
3. **Look for typos** - especially in annotations
4. **Check configuration** - OSGi, pom.xml
5. **Ask for details** - what exactly fails?

---

## Emergency Fixes

### Complete Reset

If all else fails:
```bash
# Clean everything
mvn clean
rm -rf ~/.m2/repository

# Fresh clone
cd ..
mv course course-backup
git clone <repo-url> course

# Rebuild
cd course
mvn clean install
```

---

## Getting Help

### Escalation Path

1. Check this document
2. Check troubleshooting guide
3. Search course forum
4. Ask instructor
5. Create GitHub issue

---

## Feedback for Course Improvement

Collect feedback:
1. What issues did you encounter?
2. What was unclear?
3. What would help?

Use this to improve course materials.
