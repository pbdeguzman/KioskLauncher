#!/bin/bash
# This script prompts for a selection of which CL (Compatibility Layer)
# to use for dependency setup.
#
# Usage: sh ./setup_dependencies_menu.sh

cat <<EOF
Select CL (Compatibility Layer) to Link:

 1: CLCastles
 2: CLVerifone
 3: CLNexgo
 4: CLIngenico
 5: CLSunmi

EOF

printf "Type number choice then press ENTER: "
read -re input
if [ -z ${input+x} ]; then
  echo "Invalid input. Exiting script..."
  exit 1
fi

unset compat_layer

if [[ "$input" == "1" ]]; then compat_layer="CLCastles"; fi
if [[ "$input" == "2" ]]; then compat_layer="CLVerifone"; fi
if [[ "$input" == "3" ]]; then compat_layer="CLNexgo"; fi
if [[ "$input" == "4" ]]; then compat_layer="CLIngenico"; fi
if [[ "$input" == "5" ]]; then compat_layer="CLSunmi"; fi

if [[ -z "$compat_layer" ]]; then
  echo "CL does not exist."
  exit 1
fi

if [ ! -d "../$compat_layer" ]; then
  echo "The specified '$compat_layer' dir does not exist."
  exit 1
fi

echo

sh ./setup_dependencies.sh "$compat_layer"
