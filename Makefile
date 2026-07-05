# Build and run configuration
JAVA ?= java
JAVAC ?= javac
BUILD_DIR := out
MAIN_CLASS := com.interpreter.exon.Exon

# Recursively collect Java sources without relying on shell-specific find.
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))
SOURCES := $(call rwildcard,com/,*.java)

.PHONY: all compile run clean rebuild

all: run

compile: $(SOURCES)
	@if not exist "$(BUILD_DIR)" mkdir "$(BUILD_DIR)"
	$(JAVAC) -d $(BUILD_DIR) $(SOURCES)

run: compile
	$(JAVA) -cp $(BUILD_DIR) $(MAIN_CLASS) $(ARGS)

clean:
	@if exist "$(BUILD_DIR)" rmdir /s /q "$(BUILD_DIR)"

rebuild: clean compile
