import java.util.*; 
import java.util.concurrent.*; 
// A generic interface to handle item management in the library 
interface ItemManager<T>  
{ 
void add(T item); 
void remove(T item); 
void displayAll(); 
} 
// Custom exception to handle cases when a book is not found 
class BookNotFoundException extends Exception  
{ 
    public BookNotFoundException(String message)  
{ 
        super(message); 
    } 
} 
 
// Abstract base class representing an item in the library 
abstract class LibraryItem  
{ 
    protected String id; 
    protected String title; 
    protected boolean isAvailable; 
 
    public LibraryItem(String id, String title)  
{ 
        this.id = id; 
        this.title = title; 
        this.isAvailable = true; 
    } 
 
    // Display details specific to each type of item 
    abstract void printDetails(); 
} 
 
// Book class extending the LibraryItem 
class Book extends LibraryItem  
{ 
    private String author; 
    private int publicationYear; 
 
    public Book(String id, String title, String author, int publicationYear)  
{ 
        super(id, title); 
        this.author = author; 
        this.publicationYear = publicationYear; 
    } 
 
    @Override 
    void printDetails()  
{ 
        System.out.println("Book [ID: " + id + ", Title: " + title + ", Author: " + author +  
                           ", Year: " + publicationYear + ", Available: " + isAvailable + "]"); 
    } 
} 
 
// Library class implementing generic item management 
class Library implements ItemManager<Book>  
{ 
    private List<Book> books; 
    private BlockingQueue<String> transactionLog; 
    private static final int MAX_LOG_CAPACITY = 100; 
 
    public Library()  
{ 
        this.books = new ArrayList<>(); 
        this.transactionLog = new ArrayBlockingQueue<>(MAX_LOG_CAPACITY); 
        initLogging(); 
    } 
 
    // Starts a background thread for logging transactions 
    private void initLogging() { 
        Thread logThread = new Thread(() ->  
{ 
            while (true)  
{ 
                try { 
                    String log = transactionLog.take(); 
                    System.out.println("Transaction Log: " + log + " - " + new Date()); 
                    Thread.sleep(1000); // Simulate delay in log processing 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                    break; 
                } 
            } 
        }); 
        logThread.setDaemon(true); 
        logThread.start(); 
    } 
 
    @Override 
    public void add(Book book)  
{ 
        books.add(book); 
        try { 
            transactionLog.put("Book added: " + book.title); 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        } 
    } 
 
    @Override 
    public void remove(Book book)  
{ 
        books.remove(book); 
        try { 
            transactionLog.put("Book removed: " + book.title); 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        } 
    } 
 
    @Override 
    public void displayAll()  
{ 
        books.forEach(Book::printDetails); 
    } 
 
    public void lendBook(String bookId) throws BookNotFoundException  
{ 
        Book book = findBook(bookId); 
        if (book.isAvailable)  
{ 
            book.isAvailable = false; 
            try { 
                transactionLog.put("Book borrowed: " + book.title); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            } 
        }  
else  
{ 
            throw new BookNotFoundException("This book is currently unavailable."); 
        } 
    } 
 
    public void returnBook(String bookId) throws BookNotFoundException { 
        Book book = findBook(bookId); 
        book.isAvailable = true; 
        try { 
            transactionLog.put("Book returned: " + book.title); 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        } 
    } 
 
    private Book findBook(String bookId) throws BookNotFoundException  
{ 
        return books.stream() 
            .filter(book -> book.id.equals(bookId)) 
            .findFirst() 
            .orElseThrow(() -> new BookNotFoundException("No book found with ID: " + bookId)); 
    } 
} 
 
// Main class to manage the library system 
public class LibrarySystem  
{ 
    public static void main(String[] args)  
{ 
        Library library = new Library(); 
        Scanner scanner = new Scanner(System.in); 
        boolean running = true; 
 
        while (running)  
{ 
            try { 
                System.out.println("\n=== Welcome to the Library System ==="); 
                System.out.println("1. Add a New Book"); 
                System.out.println("2. Show All Books"); 
                System.out.println("3. Borrow a Book"); 
                System.out.println("4. Return a Book"); 
                System.out.println("5. Exit"); 
                System.out.print("Please choose an option: "); 
 
                int choice = scanner.nextInt(); 
                scanner.nextLine(); // Clear the newline 
 
                switch (choice)  
{ 
                    case 1: 
                        System.out.print("Enter Book ID: "); 
                        String id = scanner.nextLine(); 
                        System.out.print("Enter Title: "); 
                        String title = scanner.nextLine(); 
                        System.out.print("Enter Author: "); 
                        String author = scanner.nextLine(); 
                        System.out.print("Enter Publication Year: "); 
                        int year = scanner.nextInt(); 
                         
                        library.add(new Book(id, title, author, year)); 
                        System.out.println("Book added successfully."); 
                        break; 
 
                    case 2: 
                        library.displayAll(); 
                        break; 
 
                    case 3: 
                        System.out.print("Enter Book ID to borrow: "); 
                        String borrowId = scanner.nextLine(); 
                        library.lendBook(borrowId); 
                        System.out.println("Book borrowed successfully."); 
                        break; 
 
                    case 4: 
                        System.out.print("Enter Book ID to return: "); 
                        String returnId = scanner.nextLine(); 
                        library.returnBook(returnId); 
                        System.out.println("Book returned successfully."); 
                        break; 
 
                    case 5: 
                        System.out.println("Thank you for using the Library System."); 
                        running = false; 
                        break; 
 
                    default: 
                        System.out.println("Invalid option, please try again."); 
                } 
            } catch (BookNotFoundException e) { 
                System.out.println("Error: " + e.getMessage()); 
            } catch (InputMismatchException e) { 
                System.out.println("Error: Please enter a valid number."); 
                scanner.nextLine(); // Clear invalid input 
            } catch (Exception e) { 
                System.out.println("Unexpected error: " + e.getMessage()); 
            } 
        } 
        scanner.close(); 
    } 
} 