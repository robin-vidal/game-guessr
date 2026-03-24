#!/usr/bin/env python3

import sys
from tkinter import Tk, Canvas
from PIL import Image, ImageTk

if len(sys.argv) != 2:
    print(f"Usage: {sys.argv[0]} <image>")
    sys.exit(1)

image_path = sys.argv[1]

# Load image
img = Image.open(image_path)

root = Tk()
root.title("Click to get coordinates")

# Get screen size
screen_w = root.winfo_screenwidth()
screen_h = root.winfo_screenheight()

# Compute scaling factor (keep aspect ratio)
scale = min(screen_w / img.width, screen_h / img.height, 1)

new_w = int(img.width * scale)
new_h = int(img.height * scale)

img_resized = img.resize((new_w, new_h), Image.LANCZOS)

canvas = Canvas(root, width=new_w, height=new_h)
canvas.pack()

tk_img = ImageTk.PhotoImage(img_resized)
canvas.create_image(0, 0, anchor="nw", image=tk_img)

# Click handler (map back to original image coordinates)
def on_click(event):
    orig_x = int(event.x / scale)
    orig_y = int(event.y / scale)
    print(f"Clicked at: ({orig_x}, {orig_y})")

canvas.bind("<Button-1>", on_click)

root.mainloop()
