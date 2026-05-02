import contextlib
import os
import warnings
import easyocr
import numpy as np

warnings.filterwarnings("ignore", category=UserWarning, module='torch')

with open(os.devnull, 'w') as f, contextlib.redirect_stdout(f):
    reader = easyocr.Reader(['en'], gpu=False)

def extract_book_titles(image):
    print("Starting OCR processing...")

    image_array = np.array(image)
    results = reader.readtext(image_array)

    book_titles = [text for (_, text, _) in results]
    book_titles = [title.strip() for title in book_titles if title.strip()]
    book_titles = [title for title in book_titles if len(title) > 1 and sum(c.isalnum() for c in title) > len(title) / 2]

    print(f"Detected titles: {book_titles}")
    return book_titles