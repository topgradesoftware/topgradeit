#!/bin/bash
# Enhanced Release Script for Android App
# This script provides a comprehensive release workflow

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
VERSION_SCRIPT="$PROJECT_ROOT/update_version.py"

# Functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

show_help() {
    cat << EOF
Enhanced Release Script for Android App

Usage: $0 [OPTIONS]

OPTIONS:
    -t, --type TYPE         Version bump type (patch|minor|major)
    -v, --version VERSION   Set specific version (e.g., 1.2.0)
    -c, --code CODE         Set specific version code
    -g, --git              Enable Git integration (commit + tag)
    -b, --build            Enable build automation
    -l, --changelog        Update changelog
    -d, --dry-run          Show what would be done without making changes
    -s, --skip-validation  Skip validation checks
    --validate-only        Run validations only
    --build-type TYPE      Build type (release|debug)
    --help                 Show this help message

EXAMPLES:
    $0 --type patch --git --build --changelog
    $0 --version 2.0.0 --git --build
    $0 --validate-only
    $0 --type minor --dry-run

WORKFLOW:
    1. Validate current state
    2. Update version in build.gradle
    3. Update changelog (if enabled)
    4. Create git commit and tag (if enabled)
    5. Build release bundle (if enabled)
    6. Show next steps

EOF
}

# Default values
VERSION_TYPE=""
VERSION=""
VERSION_CODE=""
GIT_ENABLED=false
BUILD_ENABLED=false
CHANGELOG_ENABLED=false
DRY_RUN=false
SKIP_VALIDATION=false
VALIDATE_ONLY=false
BUILD_TYPE="release"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            VERSION_TYPE="$2"
            shift 2
            ;;
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -c|--code)
            VERSION_CODE="$2"
            shift 2
            ;;
        -g|--git)
            GIT_ENABLED=true
            shift
            ;;
        -b|--build)
            BUILD_ENABLED=true
            shift
            ;;
        -l|--changelog)
            CHANGELOG_ENABLED=true
            shift
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -s|--skip-validation)
            SKIP_VALIDATION=true
            shift
            ;;
        --validate-only)
            VALIDATE_ONLY=true
            shift
            ;;
        --build-type)
            BUILD_TYPE="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validate arguments
if [[ -z "$VERSION_TYPE" && -z "$VERSION" && "$VALIDATE_ONLY" != true ]]; then
    log_error "Version type or specific version must be specified"
    show_help
    exit 1
fi

if [[ "$VALIDATE_ONLY" == true && ("$VERSION_TYPE" != "" || "$VERSION" != "") ]]; then
    log_error "Validate-only mode cannot be used with version updates"
    exit 1
fi

# Change to project root
cd "$PROJECT_ROOT"

# Check if Python and required files exist
if ! command -v python3 &> /dev/null; then
    log_error "Python 3 is required but not installed"
    exit 1
fi

if [[ ! -f "$VERSION_SCRIPT" ]]; then
    log_error "Version script not found: $VERSION_SCRIPT"
    exit 1
fi

if [[ ! -f "app/build.gradle" ]]; then
    log_error "build.gradle not found in app/ directory"
    exit 1
fi

# Build command arguments
PYTHON_ARGS=()

if [[ "$VALIDATE_ONLY" == true ]]; then
    PYTHON_ARGS+=("--validate-only")
    if [[ "$SKIP_VALIDATION" == true ]]; then
        PYTHON_ARGS+=("--no-validation")
    fi
else
    # Version update arguments
    if [[ -n "$VERSION_TYPE" ]]; then
        case "$VERSION_TYPE" in
            patch) PYTHON_ARGS+=("--patch") ;;
            minor) PYTHON_ARGS+=("--minor") ;;
            major) PYTHON_ARGS+=("--major") ;;
            *) log_error "Invalid version type: $VERSION_TYPE"; exit 1 ;;
        esac
    elif [[ -n "$VERSION" ]]; then
        PYTHON_ARGS+=("--version" "$VERSION")
    fi

    if [[ -n "$VERSION_CODE" ]]; then
        PYTHON_ARGS+=("--version-code" "$VERSION_CODE")
    fi

    # Feature flags
    if [[ "$GIT_ENABLED" == true ]]; then
        PYTHON_ARGS+=("--git")
    fi

    if [[ "$BUILD_ENABLED" == true ]]; then
        PYTHON_ARGS+=("--build")
        PYTHON_ARGS+=("--build-type" "$BUILD_TYPE")
    fi

    if [[ "$CHANGELOG_ENABLED" == true ]]; then
        PYTHON_ARGS+=("--changelog")
    fi

    if [[ "$DRY_RUN" == true ]]; then
        PYTHON_ARGS+=("--dry-run")
    fi

    if [[ "$SKIP_VALIDATION" == true ]]; then
        PYTHON_ARGS+=("--no-validation")
    fi
fi

# Show what will be executed
log_info "Executing release workflow..."
echo "Command: python3 $VERSION_SCRIPT ${PYTHON_ARGS[*]}"
echo

# Execute the version script
if python3 "$VERSION_SCRIPT" "${PYTHON_ARGS[@]}"; then
    log_success "Release workflow completed successfully!"
    
    if [[ "$VALIDATE_ONLY" != true && "$DRY_RUN" != true ]]; then
        echo
        log_info "Next steps:"
        if [[ "$GIT_ENABLED" == true ]]; then
            echo "  1. Push changes: git push"
            echo "  2. Push tags: git push --tags"
        fi
        if [[ "$BUILD_ENABLED" == true ]]; then
            echo "  3. Test the build: $BUILD_TYPE bundle created"
            echo "  4. Upload to Play Store: ./gradlew publishRelease"
        fi
        if [[ "$GIT_ENABLED" != true && "$BUILD_ENABLED" != true ]]; then
            echo "  1. Test the changes"
            echo "  2. Commit to git manually"
            echo "  3. Build release: ./gradlew bundleRelease"
        fi
    fi
else
    log_error "Release workflow failed!"
    exit 1
fi
