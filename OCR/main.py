import base64
import numpy as np
import cv2
from flask import Flask, request, jsonify
from ImageScanner import extract_book_titles

app = Flask(__name__)

@app.post("/ocr")
def scan():
    data = request.get_json()

    image_base64 = data.get("image_base64")
    if not image_base64:
        return jsonify({"error": "No image provided"}), 400

    image_bytes = base64.b64decode(image_base64)
    np_array = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(np_array, cv2.IMREAD_COLOR)

    if image is None:
        return jsonify({"error": "Could not decode image"}), 400

    titles = extract_book_titles(image)
    return jsonify({"texts": titles})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8001, debug=True)