class Book:
    def __init__(self, title, author, publication_year):
        self.title = title
        self.author = author
        self.publication_year = publication_year

    def get_book_info(self):
        return f"{self.title} by {self.author}, published in {self.publication_year}"
    