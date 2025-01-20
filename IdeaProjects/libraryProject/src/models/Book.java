package models;

public class Book {
    private String id;
    private String isbn;
    private String title;
    private String genre;
    private String author;
    private String imgPath;
    private String link;
    private String description;
    private boolean isAvailable;

    // Constructor with all fields
    public Book(String id, String isbn, String title, String genre, String author, String imgPath, String link, boolean isAvailable, String description) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Genre cannot be null or empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }

        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.genre = genre;
        this.author = author;
        this.imgPath = imgPath != null ? imgPath : "IdeaProjects/libraryProject/src/images/books/atomicHabits.jpg"; // Default image path
        this.link = link != null ? link : "#"; // Default link
        this.isAvailable = isAvailable;
        this.description = description;
    }

    // Constructor without ID (for auto-generated IDs)
    public Book(String isbn, String title, String genre, String author, String imgPath, String link, boolean isAvailable, String description) {
        this(null, isbn, title, genre, author, imgPath, link, isAvailable, description);
    }

    // Getters
    public String getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getAuthor() { return author; }
    public String getImgPath() { return imgPath; }
    public String getLink() { return link; }
    public boolean isAvailable() { return isAvailable; }
    public String getDescription() { return description; }


    // Setters
    public void setId(String id) { this.id = id; } // For auto-generated IDs
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setAuthor(String author) { this.author = author; }
    public void setImgPath(String imgPath) { this.imgPath = imgPath; }
    public void setLink(String link) { this.link = link; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setDescription(String description) { this.description = description; }

//    @Override
//    public String toString() {
//        return String.format(
//                "Book [ID: %s, ISBN: %s, Title: %s, Genre: %s, Author: %s, Available: %s, Description: %s]",
//                id, isbn, title, genre, author, isAvailable ? "Yes" : "No", description
//        );
//    }
}