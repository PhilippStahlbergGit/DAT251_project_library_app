import pytesseract
import numpy as np

def extract_book_titles(image):
    print("Starting OCR processing...")

    image_array = np.array(image)
    raw_text = pytesseract.image_to_string(image_array)

    lines = raw_text.split("\n")
    book_titles = [line.strip() for line in lines if line.strip()]
    book_titles = [t for t in book_titles if len(t) > 1 and sum(c.isalnum() for c in t) > len(t) / 2]

    print(f"Detected titles: {book_titles}")
    return book_titles