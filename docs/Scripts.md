# Scripts

This project includes a few helper scripts for common maintenance tasks.

## `update-version.sh`

Updates the Maven project version and refreshes common version references.

### What it does

1. Reads current version from Maven (`project.version`).
2. Sets the new version in `pom.xml` files via Maven Versions Plugin.
3. Adds a new version header at the top of `CHANGELOG.md`.
4. Replaces old version references in `README.md`.

### Usage

```bash
./update-version.sh <new-version>
```

Example:

```bash
./update-version.sh 1.0.39
```

---

## `maven-deploy.sh`

Runs Maven deploy with release profile enabled.

### Usage

```bash
./maven-deploy.sh
```

Equivalent command:

```bash
mvn deploy -Drelease=true
```

---

## `scripts/jacoco-class-coverage.py`

Prints per-class JaCoCo coverage from `jacoco.csv`.

### Prerequisite

Generate coverage data first:

```bash
mvn test
```

### Usage

From project root:

```bash
./scripts/jacoco-class-coverage.py org.example.MyClass
```

From `scripts/` folder:

```bash
cd scripts
./jacoco-class-coverage.py org.example.MyClass
```

Multiple classes:

```bash
./scripts/jacoco-class-coverage.py \
  org.morphix.lang.retry.DelayStrategy \
  org.morphix.lang.retry.delay.FixedDelayStrategy
```

List all classes available in the report:

```bash
./scripts/jacoco-class-coverage.py --list
```

Use custom CSV path:

```bash
./scripts/jacoco-class-coverage.py -c target/site/jacoco.csv org.example.MyClass
```

### Output

For each class, it prints:

- instruction coverage
- branch coverage
- line coverage
- method coverage
