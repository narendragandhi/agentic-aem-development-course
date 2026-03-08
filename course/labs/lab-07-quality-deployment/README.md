# Lab 7: Quality & Deployment (3 hours)

## Objective
Implement code quality tools, coverage reporting, and deployment best practices for AEM Cloud Service.

---

## Part 1: Code Quality Tools (1 hour)

### 1.1 Checkstyle Configuration

Create `checkstyle.xml` in project root:

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <property name="severity" value="error"/>

    <module name="TreeWalker">
        <!-- Naming conventions -->
        <module name="TypeName"/>
        <module name="MethodName"/>
        <module name="ConstantName"/>
        <module name="LocalVariableName"/>
        <module name="ParameterName"/>

        <!-- Code structure -->
        <module name="AvoidStarImport"/>
        <module name="OneTopLevelClass"/>
        <module name="NoLineWrap"/>

        <!-- Javadoc requirements -->
        <module name="MissingJavadocMethod">
            <property name="scope" value="public"/>
            <property name="allowMissingPropertyJavadoc" value="true"/>
        </module>
        <module name="MissingJavadocType">
            <property name="scope" value="public"/>
        </module>

        <!-- Best practices -->
        <module name="EmptyBlock"/>
        <module name="NeedBraces"/>
        <module name="LeftCurly"/>
        <module name="RightCurly"/>
    </module>

    <!-- File-level checks -->
    <module name="FileTabCharacter"/>
    <module name="NewlineAtEndOfFile"/>
</module>
```

### 1.2 SpotBugs Configuration

Create `spotbugs-exclude.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <!-- Exclude test classes -->
    <Match>
        <Class name="~.*Test"/>
    </Match>
    <Match>
        <Class name="~.*Spec"/>
    </Match>

    <!-- Exclude generated code -->
    <Match>
        <Package name="~.*\.generated\..*"/>
    </Match>
</FindBugsFilter>
```

### 1.3 PMD Configuration

Create `pmd-ruleset.xml`:

```xml
<?xml version="1.0"?>
<ruleset name="AEM Custom Rules"
    xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">

    <description>Custom PMD rules for AEM development</description>

    <!-- Best practices -->
    <rule ref="category/java/bestpractices.xml">
        <exclude name="JUnitTestsShouldIncludeAssert"/>
    </rule>

    <!-- Code style -->
    <rule ref="category/java/codestyle.xml">
        <exclude name="OnlyOneReturn"/>
        <exclude name="AtLeastOneConstructor"/>
    </rule>

    <!-- Design -->
    <rule ref="category/java/design.xml">
        <exclude name="LawOfDemeter"/>
    </rule>

    <!-- Error prone -->
    <rule ref="category/java/errorprone.xml">
        <exclude name="BeanMembersShouldSerialize"/>
    </rule>

    <!-- Security -->
    <rule ref="category/java/security.xml"/>
</ruleset>
```

### 1.4 Maven Plugin Configuration

Add to `core/pom.xml`:

```xml
<build>
    <plugins>
        <!-- Checkstyle -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <configLocation>../checkstyle.xml</configLocation>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
            </configuration>
            <executions>
                <execution>
                    <phase>validate</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- SpotBugs -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.8.3.0</version>
            <configuration>
                <excludeFilterFile>../spotbugs-exclude.xml</excludeFilterFile>
                <effort>Max</effort>
                <threshold>Medium</threshold>
            </configuration>
            <executions>
                <execution>
                    <phase>verify</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- PMD -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.21.2</version>
            <configuration>
                <rulesets>
                    <ruleset>../pmd-ruleset.xml</ruleset>
                </rulesets>
                <failOnViolation>true</failOnViolation>
            </configuration>
            <executions>
                <execution>
                    <phase>verify</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## Part 2: Code Coverage (45 min)

### 2.1 JaCoCo Configuration

```xml
<!-- JaCoCo for coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 2.2 Coverage Thresholds

| Metric | Minimum | Target |
|--------|---------|--------|
| Line Coverage | 80% | 90% |
| Branch Coverage | 70% | 85% |
| Method Coverage | 80% | 90% |

### 2.3 Exclude Generated Code

```xml
<configuration>
    <excludes>
        <exclude>**/generated/**</exclude>
        <exclude>**/*Model.class</exclude>
    </excludes>
</configuration>
```

---

## Part 3: Javadoc Standards (30 min)

### 3.1 Class-Level Documentation

```java
/**
 * Service for scanning assets for security threats.
 *
 * <p>This service implements comprehensive security scanning including:
 * <ul>
 *   <li>XSS pattern detection in metadata</li>
 *   <li>SQL injection pattern detection</li>
 *   <li>File type validation using magic bytes</li>
 *   <li>Document-specific scanning (PDF, Office)</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * SecurityScanResult result = scanner.scanAsset(asset);
 * if (result.shouldBlock()) {
 *     quarantine(asset);
 * }
 * }</pre>
 *
 * @see DocumentSecurityScanner
 * @see OwaspSecurityPatterns
 * @since 1.0.0
 */
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {
```

### 3.2 Method Documentation

```java
/**
 * Scans asset metadata for security threats.
 *
 * <p>Checks all string values in the metadata map against known
 * attack patterns including XSS, SQL injection, and command injection.
 *
 * @param metadata the metadata key-value pairs to scan, must not be null
 * @return list of security findings, empty if no threats detected
 * @throws NullPointerException if metadata is null
 *
 * @see SecurityFinding
 * @see #scanAsset(Asset)
 */
@Override
public List<SecurityFinding> scanMetadata(Map<String, Object> metadata) {
```

### 3.3 Generate Javadoc

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.3</version>
    <configuration>
        <doclint>all,-missing</doclint>
        <show>public</show>
        <nohelp>true</nohelp>
    </configuration>
    <executions>
        <execution>
            <id>attach-javadocs</id>
            <goals>
                <goal>jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Part 4: AEM Cloud Service Deployment (45 min)

### 4.1 Cloud Manager Pipeline

```yaml
# .cloudmanager/pipeline.yml
version: 1
pipelines:
  - name: Full Stack Pipeline
    type: fullStack
    target: dev
    trigger: onCommit
    build:
      mavenGoals: clean install
      profiles:
        - autoInstallSinglePackage
    codeQuality:
      enabled: true
      breakOnFailure: true
    functionalTests:
      enabled: true
```

### 4.2 Dispatcher Configuration

```
# dispatcher/src/conf.d/rewrites/rewrite.rules
RewriteRule ^/content/dam/(.*)$ - [L]

# Block direct access to quarantine folder
RewriteRule ^/content/dam/quarantine/(.*)$ - [F,L]
```

### 4.3 Environment-Specific Configuration

```java
@Component(service = SecurityConfig.class, immediate = true)
@Designate(ocd = SecurityConfig.Config.class)
public class SecurityConfig {

    @ObjectClassDefinition(name = "Security Scanner Configuration")
    public @interface Config {

        @AttributeDefinition(name = "Quarantine Path")
        String quarantinePath() default "/content/dam/quarantine";

        @AttributeDefinition(name = "Max Scan Size (MB)")
        int maxScanSizeMb() default 50;

        @AttributeDefinition(name = "Enable Deep Scan")
        boolean deepScanEnabled() default true;
    }

    private Config config;

    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
    }

    public String getQuarantinePath() {
        return config.quarantinePath();
    }
}
```

### 4.4 Runmode Configuration

Create `ui.config/src/main/content/jcr_root/apps/demo-workflow/osgiconfig/`:

```
config/                           # All environments
config.author/                    # Author only
config.publish/                   # Publish only
config.dev/                       # Dev environment
config.stage/                     # Stage environment
config.prod/                      # Production
```

---

## Part 5: CI/CD Best Practices (15 min)

### 5.1 Build Verification

```bash
# Full quality check
mvn clean verify -Pquality

# Quick build (skip quality)
mvn clean install -DskipTests -Dcheckstyle.skip -Dpmd.skip -Dspotbugs.skip
```

### 5.2 Quality Profile

```xml
<profiles>
    <profile>
        <id>quality</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-checkstyle-plugin</artifactId>
                </plugin>
                <plugin>
                    <groupId>com.github.spotbugs</groupId>
                    <artifactId>spotbugs-maven-plugin</artifactId>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-pmd-plugin</artifactId>
                </plugin>
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### 5.3 Pre-commit Hooks

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
echo "Running quality checks..."
mvn checkstyle:check pmd:check spotbugs:check -pl core -q

if [ $? -ne 0 ]; then
    echo "Quality check failed. Please fix issues before committing."
    exit 1
fi
```

---

## Verification Checklist

- [ ] Checkstyle configured and passing
- [ ] SpotBugs configured and passing
- [ ] PMD configured and passing
- [ ] JaCoCo coverage >80%
- [ ] Javadoc generated for public APIs
- [ ] Cloud Manager pipeline configured
- [ ] OSGi configurations per environment
- [ ] Pre-commit hooks installed

---

## Run Quality Checks

```bash
# Individual checks
mvn checkstyle:check -pl core
mvn spotbugs:check -pl core
mvn pmd:check -pl core

# Coverage report
mvn verify -pl core
open core/target/site/jacoco/index.html

# Generate Javadoc
mvn javadoc:javadoc -pl core
open core/target/site/apidocs/index.html

# Full quality profile
mvn clean verify -Pquality
```

---

## Next Lab
[Lab 8: Capstone Project](../lab-08-capstone/README.md)
