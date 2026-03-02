# Lab 15: Code Quality, Linting, Coverage & Documentation

## Objective
Implement comprehensive code quality tools including static analysis (linting), code coverage, code hygiene checks, and automated Javadoc generation to ensure production-ready code.

---

## Prerequisites
- Completed Labs 01-14
- Maven basics
- Understanding of code quality metrics

---

## Learning Outcomes
After completing this lab, you will be able to:
1. Configure Checkstyle for code style enforcement
2. Set up SpotBugs for bug detection
3. Implement JaCoCo for code coverage
4. Configure PMD for code analysis
5. Generate and validate Javadocs
6. Create quality gates that fail builds

---

## Part 1: Code Quality Tool Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    CODE QUALITY PIPELINE                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │Checkstyle│  │ SpotBugs │  │   PMD    │  │  JaCoCo  │        │
│  │  Style   │  │   Bugs   │  │ Analysis │  │ Coverage │        │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │
│       │             │             │             │               │
│       ▼             ▼             ▼             ▼               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  QUALITY GATE                            │   │
│  │  • Style violations: 0                                   │   │
│  │  • Bug patterns: 0 HIGH                                  │   │
│  │  • Code coverage: ≥80%                                   │   │
│  │  • PMD violations: 0 CRITICAL                            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Part 2: Checkstyle - Code Style Enforcement

### 2.1 Add Checkstyle Plugin

Add to `core/pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle/checkstyle.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
    </configuration>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>com.puppycrawl.tools</groupId>
            <artifactId>checkstyle</artifactId>
            <version>10.12.5</version>
        </dependency>
    </dependencies>
</plugin>
```

### 2.2 Create Checkstyle Configuration

Create `core/checkstyle/checkstyle.xml`:

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="error"/>
    <property name="fileExtensions" value="java"/>

    <!-- File length -->
    <module name="FileLength">
        <property name="max" value="500"/>
    </module>

    <!-- No tabs -->
    <module name="FileTabCharacter"/>

    <!-- Trailing whitespace -->
    <module name="RegexpSingleline">
        <property name="format" value="\s+$"/>
        <property name="message" value="Line has trailing whitespace"/>
    </module>

    <module name="TreeWalker">
        <!-- Naming conventions -->
        <module name="ConstantName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="TypeName"/>

        <!-- Imports -->
        <module name="AvoidStarImport"/>
        <module name="IllegalImport"/>
        <module name="RedundantImport"/>
        <module name="UnusedImports"/>

        <!-- Size violations -->
        <module name="LineLength">
            <property name="max" value="120"/>
            <property name="ignorePattern" value="^package.*|^import.*"/>
        </module>
        <module name="MethodLength">
            <property name="max" value="50"/>
        </module>
        <module name="ParameterNumber">
            <property name="max" value="7"/>
        </module>

        <!-- Whitespace -->
        <module name="EmptyForIteratorPad"/>
        <module name="GenericWhitespace"/>
        <module name="MethodParamPad"/>
        <module name="NoWhitespaceAfter"/>
        <module name="NoWhitespaceBefore"/>
        <module name="ParenPad"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>

        <!-- Coding -->
        <module name="EmptyStatement"/>
        <module name="EqualsHashCode"/>
        <module name="IllegalInstantiation"/>
        <module name="InnerAssignment"/>
        <module name="MissingSwitchDefault"/>
        <module name="SimplifyBooleanExpression"/>
        <module name="SimplifyBooleanReturn"/>

        <!-- Design -->
        <module name="FinalClass"/>
        <module name="HideUtilityClassConstructor"/>
        <module name="InterfaceIsType"/>

        <!-- Javadoc -->
        <module name="JavadocMethod">
            <property name="accessModifiers" value="public"/>
        </module>
        <module name="JavadocType"/>
        <module name="MissingJavadocMethod">
            <property name="scope" value="public"/>
            <property name="allowMissingPropertyJavadoc" value="true"/>
        </module>
    </module>
</module>
```

### 2.3 Run Checkstyle

```bash
mvn checkstyle:check -pl core
```

---

## Part 3: SpotBugs - Bug Detection

### 3.1 Add SpotBugs Plugin

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.2.0</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Medium</threshold>
        <failOnError>true</failOnError>
        <xmlOutput>true</xmlOutput>
        <excludeFilterFile>spotbugs/spotbugs-exclude.xml</excludeFilterFile>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 3.2 Create SpotBugs Exclusion File

Create `core/spotbugs/spotbugs-exclude.xml`:

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

    <!-- Allow some patterns in specific classes -->
    <Match>
        <Class name="com.demo.workflow.services.impl.OwaspSecurityPatterns"/>
        <Bug pattern="URF_UNREAD_FIELD"/>
    </Match>
</FindBugsFilter>
```

### 3.3 Run SpotBugs

```bash
mvn spotbugs:check -pl core
```

---

## Part 4: PMD - Code Analysis

### 4.1 Add PMD Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.2</version>
    <configuration>
        <sourceEncoding>UTF-8</sourceEncoding>
        <minimumTokens>100</minimumTokens>
        <targetJdk>11</targetJdk>
        <failOnViolation>true</failOnViolation>
        <printFailingErrors>true</printFailingErrors>
        <rulesets>
            <ruleset>pmd/ruleset.xml</ruleset>
        </rulesets>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
                <goal>cpd-check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 4.2 Create PMD Ruleset

Create `core/pmd/ruleset.xml`:

```xml
<?xml version="1.0"?>
<ruleset name="AEM Project Rules"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0
                             https://pmd.sourceforge.io/ruleset_2_0_0.xsd">

    <description>Custom ruleset for AEM projects</description>

    <!-- Best Practices -->
    <rule ref="category/java/bestpractices.xml">
        <exclude name="GuardLogStatement"/>
    </rule>

    <!-- Code Style -->
    <rule ref="category/java/codestyle.xml">
        <exclude name="OnlyOneReturn"/>
        <exclude name="AtLeastOneConstructor"/>
        <exclude name="CommentDefaultAccessModifier"/>
    </rule>

    <!-- Design -->
    <rule ref="category/java/design.xml">
        <exclude name="LawOfDemeter"/>
        <exclude name="LoosePackageCoupling"/>
    </rule>

    <!-- Error Prone -->
    <rule ref="category/java/errorprone.xml">
        <exclude name="BeanMembersShouldSerialize"/>
    </rule>

    <!-- Performance -->
    <rule ref="category/java/performance.xml"/>

    <!-- Security -->
    <rule ref="category/java/security.xml"/>

    <!-- Custom Rules -->
    <rule name="AvoidSystemOut"
          language="java"
          message="Avoid System.out - use SLF4J Logger instead"
          class="net.sourceforge.pmd.lang.rule.XPathRule">
        <description>Use Logger instead of System.out</description>
        <priority>2</priority>
        <properties>
            <property name="xpath">
                <value>
                    //Name[@Image='out' or @Image='err']
                    [parent::PrimaryPrefix/following-sibling::PrimarySuffix]
                    [ancestor::PrimaryExpression/PrimaryPrefix/Name[@Image='System']]
                </value>
            </property>
        </properties>
    </rule>
</ruleset>
```

### 4.3 Run PMD

```bash
mvn pmd:check pmd:cpd-check -pl core
```

---

## Part 5: JaCoCo - Code Coverage

### 5.1 Add JaCoCo Plugin

```xml
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
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
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
                    <rule>
                        <element>CLASS</element>
                        <excludes>
                            <exclude>*Test</exclude>
                            <exclude>*Spec</exclude>
                        </excludes>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 5.2 Run Coverage Analysis

```bash
mvn test jacoco:report -pl core
```

### 5.3 View Coverage Report

Open `core/target/site/jacoco/index.html` in browser.

---

## Part 6: Javadoc Generation & Validation

### 6.1 Add Javadoc Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.3</version>
    <configuration>
        <source>11</source>
        <doclint>all,-missing</doclint>
        <failOnError>true</failOnError>
        <quiet>true</quiet>
        <additionalOptions>
            <additionalOption>-Xdoclint:all</additionalOption>
            <additionalOption>-Xdoclint:-missing</additionalOption>
        </additionalOptions>
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

### 6.2 Javadoc Best Practices

```java
/**
 * Scans content for security vulnerabilities using OWASP patterns.
 *
 * <p>Provides comprehensive security scanning including:</p>
 * <ul>
 *   <li>XSS detection in metadata and content</li>
 *   <li>SQL injection pattern matching</li>
 *   <li>File type validation using magic bytes</li>
 *   <li>Embedded script detection in documents</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * SecurityScanResult result = scanner.scanAsset(asset);
 * if (result.isBlocked()) {
 *     quarantineAsset(asset);
 * }
 * }</pre>
 *
 * @author Demo Project Team
 * @version 1.0
 * @since 2024.11
 * @see SecurityScannerService
 * @see OwaspSecurityPatterns
 */
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {

    /**
     * Performs a comprehensive security scan on a DAM asset.
     *
     * <p>The scan includes:</p>
     * <ol>
     *   <li>Metadata analysis for injection patterns</li>
     *   <li>File type validation (header vs extension)</li>
     *   <li>Content scanning for embedded scripts</li>
     *   <li>Document-specific scanning (PDF, Office)</li>
     * </ol>
     *
     * @param asset The DAM asset to scan. Must not be {@code null}.
     * @return A {@link SecurityScanResult} containing all findings
     *         and the overall threat assessment.
     * @throws IllegalArgumentException if asset is null
     * @see #scanMetadata(Map)
     * @see #validateFileType(InputStream, String, String)
     */
    @Override
    public SecurityScanResult scanAsset(Asset asset) {
        // implementation
    }
}
```

### 6.3 Generate Javadocs

```bash
mvn javadoc:javadoc -pl core
```

### 6.4 View Javadocs

Open `core/target/site/apidocs/index.html` in browser.

---

## Part 7: Create Quality Profile

### 7.1 Combined Quality Check

Add to `pom.xml`:

```xml
<profile>
    <id>quality-check</id>
    <build>
        <plugins>
            <!-- All quality plugins activated -->
        </plugins>
    </build>
</profile>
```

### 7.2 Run Full Quality Check

```bash
mvn clean verify -Pquality-check -pl core
```

### 7.3 Quality Gate Requirements

| Metric | Threshold | Tool |
|--------|-----------|------|
| Checkstyle violations | 0 | Checkstyle |
| SpotBugs HIGH/CRITICAL | 0 | SpotBugs |
| PMD violations | 0 CRITICAL | PMD |
| Line coverage | ≥80% | JaCoCo |
| Branch coverage | ≥70% | JaCoCo |
| Javadoc errors | 0 | Maven Javadoc |
| Duplicate code | <5% | PMD CPD |

---

## Part 8: CI/CD Integration

### 8.1 GitHub Actions Workflow

Create `.github/workflows/quality.yml`:

```yaml
name: Code Quality

on: [push, pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Run Checkstyle
        run: mvn checkstyle:check -pl core

      - name: Run SpotBugs
        run: mvn spotbugs:check -pl core

      - name: Run PMD
        run: mvn pmd:check pmd:cpd-check -pl core

      - name: Run Tests with Coverage
        run: mvn test jacoco:report -pl core

      - name: Check Coverage Threshold
        run: mvn jacoco:check -pl core

      - name: Generate Javadocs
        run: mvn javadoc:javadoc -pl core

      - name: Upload Coverage Report
        uses: actions/upload-artifact@v3
        with:
          name: coverage-report
          path: core/target/site/jacoco/
```

---

## Verification Checklist

- [ ] Checkstyle configured with custom rules
- [ ] SpotBugs detecting potential bugs
- [ ] PMD analyzing code quality
- [ ] JaCoCo showing ≥80% coverage
- [ ] Javadocs generating without errors
- [ ] Quality gate failing on violations
- [ ] CI/CD pipeline integrated

---

## Exercise: Fix Quality Issues

1. Run `mvn checkstyle:check -pl core`
2. Fix all style violations
3. Run `mvn spotbugs:check -pl core`
4. Fix all bug patterns
5. Run `mvn test jacoco:report -pl core`
6. Add tests until coverage ≥80%
7. Run `mvn javadoc:javadoc -pl core`
8. Add missing Javadocs

---

## Bonus Challenges

1. **Add SonarQube:** Integrate with SonarCloud for comprehensive analysis
2. **Add Mutation Testing:** Use PITest for mutation coverage
3. **Add Architecture Tests:** Use ArchUnit for architecture validation
4. **Add License Headers:** Use license-maven-plugin

---

## References

- [Checkstyle](https://checkstyle.org/)
- [SpotBugs](https://spotbugs.github.io/)
- [PMD](https://pmd.github.io/)
- [JaCoCo](https://www.jacoco.org/jacoco/)
- [Maven Javadoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/)
