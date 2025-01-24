package services;

import models.Book;
import utils.CSVUtils;

import java.util.*;
import java.util.stream.Collectors;

public class BookService {
    private static final  String HEADER_CSV = "id,isbn,title,genre,author,imgPath,link,isAvailable,description";
    private static final String BOOKS_CSV = "IdeaProjects/libraryProject/src/data/booksInfo.csv";
    private static final int ID_INDEX = 0;
    private static final int ISBN_INDEX = 1;
    private static final int TITLE_INDEX = 2;
    private static final int GENRE_INDEX = 3;
    private static final int AUTHOR_INDEX = 4;
    private static final int IMG_PATH_INDEX = 5;
    private static final int LINK_INDEX = 6;
    private static final int IS_AVAILABLE_INDEX = 7;
    private static final int DESCRIPTION_INDEX = 8;

    private final Map<String, Book> bookMap; // HashMap for fast lookup (id as key)
    private final List<Book> bookList;      // ArrayList to store books in order
    private int nextId;                     // Track the next available ID

    public BookService() {
        this.bookMap = new HashMap<>();
        this.bookList = new ArrayList<>();
        this.nextId = 1; // Start IDs from 1
        loadBooks(); // Load books from CSV file when the service is initialized
    }

    public Map<String, Integer> getGenreCounts() {
        Map<String, Integer> genreCounts = new HashMap<>();
        for (Book book : bookList) {
            String genre = book.getGenre();
            genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
        }
        return genreCounts;
    }
    private void loadBooks() {
        List<String[]> rows = CSVUtils.readCSV(BOOKS_CSV);
        for (String[] row : rows) {
            if (row.length >= 9) { // Ensure the row has all required fields
                try {
                    Book book = new Book(
                            row[ID_INDEX],
                            row[ISBN_INDEX],
                            row[TITLE_INDEX],
                            row[GENRE_INDEX],
                            row[AUTHOR_INDEX],
                            row[IMG_PATH_INDEX],
                            row[LINK_INDEX],
                            Boolean.parseBoolean(row[IS_AVAILABLE_INDEX]),
                            row[DESCRIPTION_INDEX]
                    );
                    bookMap.put(book.getId(), book); // Add to HashMap
                    bookList.add(book); // Add to ArrayList

                    // Update nextId to ensure it's always greater than the highest existing ID
                    int bookId = Integer.parseInt(book.getId());
                    if (bookId >= nextId) {
                        nextId = bookId + 1;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing row: " + String.join(",", row));
                    e.printStackTrace();
                }
            } else {
                System.err.println("Invalid row in CSV: " + String.join(",", row));
            }
        }
        System.out.println("Total Books: "+getTotalBooks());
    }

    /**
     * Add a new book with an auto-generated ID.
     *
     * @param newBook The book to add.
     * @return True if the book was added successfully, false otherwise.
     */
    public boolean addBook(Book newBook) {
        if (newBook == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        // Validate required fields
        if (newBook.getIsbn() == null || newBook.getIsbn().trim().isEmpty() ||
                newBook.getTitle() == null || newBook.getTitle().trim().isEmpty() ||
                newBook.getGenre() == null || newBook.getGenre().trim().isEmpty() ||
                newBook.getAuthor() == null || newBook.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN, Title, Genre, and Author are required fields");
        }

        // Generate a new ID for the book
        String newId = String.valueOf(nextId);
        newBook.setId(newId); // Set the auto-generated ID

        if (bookMap.containsKey(newId)) {
            return false; // Book already exists (should not happen with auto-generated IDs)
        }

        bookMap.put(newId, newBook);
        bookList.add(newBook);
        nextId++; // Increment the next available ID
        saveBooks(); // Save to CSV
        return true;
    }

    /**
     * Save all books to the CSV file.
     */
    public void saveBooks() {
        List<String[]> data = bookList.stream()
                .map(book -> new String[]{
                        book.getId(),
                        book.getIsbn(),
                        book.getTitle(),
                        book.getGenre(),
                        book.getAuthor(),
                        book.getImgPath(),
                        book.getLink(),
                        String.valueOf(book.isAvailable()),
                        book.getDescription()
                })
                .collect(Collectors.toList());

        // Write to CSV
        CSVUtils.updateCSV(BOOKS_CSV, data,HEADER_CSV);
    }

    /**
     * Get all books.
     *
     * @return A list of all books.
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(bookList); // Return a copy to prevent external modification
    }
    public int getTotalBooks(){
        return bookList.size();
    }
    public int getTotalBorrowedBook(){
        return bookList.size()-getTotalAvailableBooks();
    }

    /**
     * Get a book by its ID.
     *
     * @param id The ID of the book.
     * @return The book with the specified ID, or null if not found.
     */
    public Book getBookById(String id) {
        return bookMap.get(id);
    }

    /**
     * Update a book's details.
     *
     * @param updatedBook The updated book object.
     * @return True if the book was updated successfully, false otherwise.
     */
    public boolean updateBook(Book updatedBook) {
        if (updatedBook == null || !bookMap.containsKey(updatedBook.getId())) {
            return false; // Book does not exist
        }


        bookMap.put(updatedBook.getId(), updatedBook);
        bookList.replaceAll(book -> book.getId().equals(updatedBook.getId()) ? updatedBook : book);
        saveBooks(); // Save changes to CSV
        return true;
    }
//    public boolean deleteBook(String id) {
//        if (!bookMap.containsKey(id)) {
//            return false; // Book does not exist
//        }
//
//        // Remove the book from the map and list
//        bookMap.remove(id);
//        bookList.removeIf(book -> book.getId().equals(id));
//        saveBooks(); // Save changes to CSV
//        return true;
//    }
    public boolean deleteBook(String isbn) {
        Book bookToDelete = findBookByISBN(isbn);
        if (bookToDelete == null) return false;

        bookMap.remove(bookToDelete.getId());
        bookList.removeIf(b -> b.getId().equals(bookToDelete.getId()));
        saveBooks();
        return true;
    }

    public List<Book> getBooksByGenre(String genre) {
        return bookList.stream()
                .filter(book -> book.getGenre().equalsIgnoreCase(genre))
                .collect(Collectors.toList());
    }

    /**
     * Get books by author.
     *
     * @param author The author to filter by.
     * @return A list of books by the specified author.
     */
    public List<Book> getBooksByAuthor(String author) {
        return bookList.stream()
                .filter(book -> book.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    /**
     * Get all unique genres.
     *
     * @return A list of all unique genres.
     */
    public List<String> getUniqueGenres() {
        return bookList.stream()
                .map(Book::getGenre)
                .distinct()
                .collect(Collectors.toList());
    }
    public String getBookTitleById(String id){
        Book book=bookMap.get(id);
        return book!=null?book.getTitle() : "Unknown";
    }
    public List<String> getUniqueAuthors() {
        return bookList.stream()
                .map(Book::getAuthor)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<Book> getAvailableBooks() {
        return bookList.stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }
    public int getTotalAvailableBooks() {
        int count = 0;
        for (Book book : bookList) {
            if (book.isAvailable()) {
                count++;
            }
        }
        return count;
    }
    public Book getBookByIsbn(String isbn) {
        return findBookByISBN(isbn); // Implement ISBN-based lookup
    }
    private Book findBookByISBN(String isbn) {
        return bookList.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst()
                .orElse(null);
    }
    public boolean updateBook(String originalISBN, Book updatedBook) {
        Book existingBook = findBookByISBN(originalISBN);
        if (existingBook == null) return false;

        String newISBN = updatedBook.getIsbn();

        // Check for ISBN conflict (if ISBN changed)
        if (!originalISBN.equals(newISBN)) {
            if (findBookByISBN(newISBN) != null) return false;
        }

        // Preserve original ID
        updatedBook.setId(existingBook.getId());

        // Update data structures
        bookMap.put(updatedBook.getId(), updatedBook);
        bookList.replaceAll(b -> b.getId().equals(existingBook.getId()) ? updatedBook : b);

        saveBooks();
        return true;
    }
}