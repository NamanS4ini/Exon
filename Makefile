# Build and run configuration
JAVA ?= java
JAVAC ?= javac
BUILD_DIR := out
MAIN_CLASS := com.interpreter.exon.Exon

ifeq ($(OS),Windows_NT)
MKDIR_BUILD = if not exist "$(BUILD_DIR)" mkdir "$(BUILD_DIR)"
RMDIR_BUILD = if exist "$(BUILD_DIR)" rmdir /s /q "$(BUILD_DIR)"
else
MKDIR_BUILD = mkdir -p "$(BUILD_DIR)"
RMDIR_BUILD = rm -rf "$(BUILD_DIR)"
endif

# Recursively collect Java sources without relying on shell-specific find.
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))
SOURCES := $(call rwildcard,com/,*.java)
TEST_FILE := test.exon

.PHONY: all compile run clean rebuild generateast test

all: run

compile: $(SOURCES)
	@$(MKDIR_BUILD)
	$(JAVAC) -d $(BUILD_DIR) $(SOURCES)

run: compile
	$(JAVA) -cp $(BUILD_DIR) $(MAIN_CLASS) $(ARGS)

generateast: compile
	$(JAVA) -cp $(BUILD_DIR) com.interpreter.tool.generateAst com/interpreter/exon

test: compile
	$(JAVA) -cp $(BUILD_DIR) $(MAIN_CLASS) $(TEST_FILE)

clean:
	@$(RMDIR_BUILD)

rebuild: clean compile
