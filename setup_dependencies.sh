#!/bin/bash
# Usage: sh ./setup_dependencies.sh <CLRepoName>

cat <<EOF
This script will perform some directory cleanup and create new symbolic
links for Nucleus UPA dependencies.

You need to clone the FBs (Functional Blocks) and at least one
CL (Compatibility Layer) repo beforehand.

See Google Doc "Nucleus: Developer's Guide" Section 3.2 Payment Interface Application
for more details.

EOF

printf "Proceed with dependency configuration? (y/N) > "
read -re input
if [[ "$input,," != "y"* ]]; then
  echo "Bye-bye. Exiting..."
  exit 0
fi

echo

SUBMODULES_NAME='submodules'
SUBMODULES_DIR="./$SUBMODULES_NAME"

if [[ $# -eq 0 ]]; then
  echo 'Please provide CL (Compatibility Layer) repo name.'
  exit 1
fi

# A command line argument is required to specify which
# CL (Compatibility Layer) should be used.
CL_REPO_NAME="$1"
CL_REPO_REL_PATH="../$1"

if [ ! -d "$CL_REPO_REL_PATH" ]; then
  echo "The specified '$CL_REPO_REL_PATH' dir does not exist."
  exit 1
fi

# Check if the submodules directory already exists.
if [ -d "$SUBMODULES_DIR" ]; then
  echo "Deleting existing '$SUBMODULES_NAME' directory..."
  rm -r "$SUBMODULES_DIR"
fi

echo "Recreating links..."
echo "Creating '$SUBMODULES_NAME' directory..."
mkdir "$SUBMODULES_DIR"
echo

FB_REPO_REL_PATH="../FBs"

if [ ! -d "$FB_REPO_REL_PATH" ]; then
  echo "The '$FB_REPO_REL_PATH' repo does not exist. Clone the repo and then try again."
  exit 1
fi

# The submodules dir is one level deeper, so we need to link the destination
# one level higher (same for the below CL repo).
echo "Creating symbolic link for FBs (Functional Blocks)..."
ln -s "../$FB_REPO_REL_PATH" "$SUBMODULES_DIR/fb"
echo "Creating symbolic link for $CL_REPO_NAME (Compatibility Layer)..."
ln -s "../$CL_REPO_REL_PATH" "$SUBMODULES_DIR/cl"

echo 'Modifying app/build.gradle'
echo

# Remove the 'CL' prefix. Ex: CLCastles => Castles
# DOS equivalent: %cl:~2%
VENDOR_NAME_REPLACEMENT="${CL_REPO_NAME:2}"
# Remove the 'CL' prefix and use just the first letter. Ex: CLVerifone => V
# DOS equivalent: %cl:~2,1%
VENDOR_SUFFIX_REPLACEMENT="${CL_REPO_NAME:2:1}"

sed -i '' "s/VENDOR_NAME/$VENDOR_NAME_REPLACEMENT/g" ./app/build.gradle
sed -i '' "s/VENDOR_SUFFIX/$VENDOR_SUFFIX_REPLACEMENT/g" ./app/build.gradle

echo
echo "Finished setting up dependencies!"
echo
