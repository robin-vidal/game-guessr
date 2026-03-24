#!/usr/bin/env bash

# Exit if no webp files are found
shopt -s nullglob

for file in *.webp; do
    # Remove extension and add .png
    output="${file%.webp}.png"
    
    echo "Converting $file -> $output"
    magick "$file" "$output"
done

echo "Done."
