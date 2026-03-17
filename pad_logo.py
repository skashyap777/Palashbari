from PIL import Image
import os

def process_logo():
    input_path = 'assets/Logo.png'
    output_path = 'assets/Logo_v2.png'
    
    if not os.path.exists(input_path):
        print(f"Error: {input_path} not found.")
        return

    img = Image.open(input_path).convert("RGBA")
    
    # Check current size
    w, h = img.size
    print(f"Original size: {w}x{h}")

    # Android 12 circle mask diameter is roughly the full icon size.
    # To fit a square/squircle completely inside a circle, the circle diameter 
    # must be at least the diagonal of the square.
    # Diagonal = sqrt(w^2 + h^2) ~= 1.414 * w.
    # So we need at least ~1.42x padding. Let's do 1.6x to be safe and give some breathing room.
    
    scale_factor = 2.5
    new_w = int(w * scale_factor)
    new_h = int(h * scale_factor)
    
    # Create new transparent background
    new_img = Image.new("RGBA", (new_w, new_h), (0, 0, 0, 0))
    
    # Paste original in center
    offset_x = (new_w - w) // 2
    offset_y = (new_h - h) // 2
    new_img.paste(img, (offset_x, offset_y), img)
    
    new_img.save(output_path, "PNG")
    print(f"Saved padded logo to {output_path} ({new_w}x{new_h})")

if __name__ == "__main__":
    process_logo()
