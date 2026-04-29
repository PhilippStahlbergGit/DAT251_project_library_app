import contextlib
import os
import warnings

import easyocr
import numpy as np

# Suppress PyTorch warnings
warnings.filterwarnings("ignore", category=UserWarning, module='torch')

# Initialize the reader once (English language)
with open(os.devnull, 'w') as f, contextlib.redirect_stdout(f):
    reader = easyocr.Reader(['en'], gpu=False)

def extract_book_titles(image):
    """
    Takes an OpenCV Image of a bookshelf and returns a list of guessed book titles
    """
    
    print("Starting OCR processing to extract book titles...")

    # Convert OpenCV Image to NumPy array
    image_array = np.array(image)
    # print("Image converted to NumPy array for OCR processing.")

    # Perform OCR
    results = reader.readtext(image_array)
    # print(f"OCR results: {results}")

    # Extract the detected text
    book_titles = [text for (_, text, _) in results]
    # print(f"Raw detected book titles: {book_titles}")


    #Filtering
    print("Filtering detected book titles...")

    # Clean empty or whitespace-only results
    book_titles = [title.strip() for title in book_titles if title.strip()]

    # Filter out results that are too short or contain mostly non-alphanumeric characters
    book_titles = [title for title in book_titles if len(title) > 1 and sum(c.isalnum() for c in title) > len(title) / 2]

    # print(f"Filtered detected book titles: {book_titles}")
    return book_titles