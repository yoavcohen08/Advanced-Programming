package smarticulous;

import smarticulous.db.Exercise;
import smarticulous.db.Submission;
import smarticulous.db.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Smarticulous class, implementing a grading system.
 */
public class Smarticulous {

    /**
     * The connection to the underlying DB.
     * <p>
     * null if the db has not yet been opened.
     */
    Connection db;

    /**
     * Open the {@link Smarticulous} SQLite database.
     * <p>
     * This should open the database, creating a new one if necessary, and set the {@link #db} field
     * to the new connection.
     * <p>
     * The open method should make sure the database contains the following tables, creating them if necessary:
     *
     * <table>
     *   <caption><em>Table name: <strong>User</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>UserId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>Username</td><td>Text</td></tr>
     *   <tr><td>Firstname</td><td>Text</td></tr>
     *   <tr><td>Lastname</td><td>Text</td></tr>
     *   <tr><td>Password</td><td>Text</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Exercise</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>ExerciseId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>Name</td><td>Text</td></tr>
     *   <tr><td>DueDate</td><td>Integer</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Question</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>ExerciseId</td><td>Integer</td></tr>
     *   <tr><td>QuestionId</td><td>Integer</td></tr>
     *   <tr><td>Name</td><td>Text</td></tr>
     *   <tr><td>Desc</td><td>Text</td></tr>
     *   <tr><td>Points</td><td>Integer</td></tr>
     * </table>
     * In this table the combination of ExerciseId and QuestionId together comprise the primary key.
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>Submission</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>SubmissionId</td><td>Integer (Primary Key)</td></tr>
     *   <tr><td>UserId</td><td>Integer</td></tr>
     *   <tr><td>ExerciseId</td><td>Integer</td></tr>
     *   <tr><td>SubmissionTime</td><td>Integer</td></tr>
     * </table>
     *
     * <p>
     * <table>
     *   <caption><em>Table name: <strong>QuestionGrade</strong></em></caption>
     *   <tr><th>Column</th><th>Type</th></tr>
     *   <tr><td>SubmissionId</td><td>Integer</td></tr>
     *   <tr><td>QuestionId</td><td>Integer</td></tr>
     *   <tr><td>Grade</td><td>Real</td></tr>
     * </table>
     * In this table the combination of SubmissionId and QuestionId together comprise the primary key.
     *
     * @param dburl The JDBC url of the database to open (will be of the form "jdbc:sqlite:...")
     * @return the new connection
     * @throws SQLException
     */
    public Connection openDB(String dburl) throws SQLException
    {
        db = DriverManager.getConnection(dburl);
        CheckIfTablesExist();
        return db;
    }
    private void CheckIfTablesExist() throws SQLException {
        String[] tables = {
                // User table
                "CREATE TABLE IF NOT EXISTS User (" +
                        "UserId INTEGER PRIMARY KEY," +
                        "Username TEXT NOT NULL UNIQUE," +
                        "Firstname TEXT," +
                        "Lastname TEXT," +
                        "Password TEXT NOT NULL" +
                        ");",

                // Exercise table
                "CREATE TABLE IF NOT EXISTS Exercise (" +
                        "ExerciseId INTEGER PRIMARY KEY," +
                        "Name TEXT NOT NULL," +
                        "DueDate INTEGER" +
                        ");",

                // Question table
                "CREATE TABLE IF NOT EXISTS Question (" +
                        "ExerciseId INTEGER," +
                        "QuestionId INTEGER," +
                        "Name TEXT NOT NULL," +
                        "Desc TEXT," +
                        "Points INTEGER," +
                        "PRIMARY KEY (ExerciseId, QuestionId)," +
                        "FOREIGN KEY (ExerciseId) REFERENCES Exercise(ExerciseId)" +
                        ");",

                // Submission table
                "CREATE TABLE IF NOT EXISTS Submission (" +
                        "SubmissionId INTEGER PRIMARY KEY," +
                        "UserId INTEGER," +
                        "ExerciseId INTEGER," +
                        "SubmissionTime INTEGER," +
                        "FOREIGN KEY (UserId) REFERENCES User(UserId)," +
                        "FOREIGN KEY (ExerciseId) REFERENCES Exercise(ExerciseId)" +
                        ");",

                // QuestionGrade table
                "CREATE TABLE IF NOT EXISTS QuestionGrade (" +
                        "SubmissionId INTEGER," +
                        "QuestionId INTEGER," +
                        "Grade REAL," +
                        "PRIMARY KEY (SubmissionId, QuestionId)," +
                        "FOREIGN KEY (SubmissionId) REFERENCES Submission(SubmissionId)," +
                        "FOREIGN KEY (QuestionId) REFERENCES Question(QuestionId)" +
                        ");"
        };
        Statement stmt = db.createStatement();
        for (String table : tables) {
            stmt.execute(table);
        }
    }


    /**
     * Close the DB if it is open.
     *
     * @throws SQLException
     */
    public void closeDB() throws SQLException {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    // =========== User Management =============

    /**
     * Add a user to the database / modify an existing user.
     * <p>
     * Add the user to the database if they don't exist. If a user with user.username does exist,
     * update their password and firstname/lastname in the database.
     *
     * @param user
     * @param password
     * @return the userid.
     * @throws SQLException
     */
    public int addOrUpdateUser(User user, String password) throws SQLException {
        // Step 1: Check if the user exists
        String selectQuery = "SELECT UserId FROM User WHERE Username = ?";
        try (PreparedStatement selectStmt = db.prepareStatement(selectQuery)) {
            selectStmt.setString(1, user.username);
            try (ResultSet result = selectStmt.executeQuery()) {
                if (result.next()) {
                    // Step 2: If the user exists, update their password and firstname/lastname in the database.
                    int userId = result.getInt("UserId");
                    String updateQuery = "UPDATE User SET Firstname = ?, Lastname = ?, Password = ? WHERE UserId = ?";
                    try (PreparedStatement updateStmt = db.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, user.firstname);
                        updateStmt.setString(2, user.lastname);
                        updateStmt.setString(3, password);
                        updateStmt.setInt(4, userId);
                        updateStmt.executeUpdate();
                    }
                    return userId;
                }
            }
        }
        // Step 3: If the user does not exist
        String insertQuery = "INSERT INTO User (Username, Firstname, Lastname, Password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = db.prepareStatement(insertQuery)) {
            insertStmt.setString(1, user.username);
            insertStmt.setString(2, user.firstname);
            insertStmt.setString(3, user.lastname);
            insertStmt.setString(4, password);
            insertStmt.executeUpdate();
            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;  // If everything fails, return -1
    }


    /**
     * Verify a user's login credentials.
     *
     * @param username
     * @param password
     * @return true if the user exists in the database and the password matches; false otherwise.
     * @throws SQLException
     * <p>
     * Note: this is totally insecure. For real-life password checking, it's important to store only
     * a password hash
     * @see <a href="https://crackstation.net/hashing-security.htm">How to Hash Passwords Properly</a>
     */
    public boolean verifyLogin(String username, String password) throws SQLException {
        String sel = "SELECT UserId FROM User WHERE Username = ? AND Password = ?";
        try (PreparedStatement stmt = db.prepareStatement(sel)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next();
            }
        }
    }

    // =========== Exercise Management =============

    /**
     * Add an exercise to the database.
     *
     * @param exercise
     * @return the new exercise id, or -1 if an exercise with this id already existed in the database.
     * @throws SQLException
     */
    public int addExercise(Exercise exercise) throws SQLException {
        // Step 1: Check if the exercise already exists
        String check = "SELECT ExerciseId FROM Exercise WHERE ExerciseId = ?";
        try (PreparedStatement checkStmt = db.prepareStatement(check)) {
            checkStmt.setInt(1, exercise.id);
            try (ResultSet result = checkStmt.executeQuery()) {
                if (result.next()) {
                    // Exercise already exists
                    return -1;
                }
            }
        }
        //Step 2: Insert the Exercise
        String insertEx = "INSERT INTO Exercise (ExerciseId, Name, DueDate) VALUES (?, ?, ?)";
        try (PreparedStatement insertExStmt = db.prepareStatement(insertEx)) {
            insertExStmt.setInt(1, exercise.id);
            insertExStmt.setString(2, exercise.name);
            insertExStmt.setLong(3, exercise.dueDate.getTime());
            insertExStmt.executeUpdate();
        }
        //Step 3: Insert the Questions
        String insertQ = "INSERT INTO Question (ExerciseId, QuestionId, Name, Desc, Points) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertQStmt = db.prepareStatement(insertQ)) {
            int questionId = 1;
            for (Exercise.Question question : exercise.questions) {
                insertQStmt.setInt(1, exercise.id);
                insertQStmt.setInt(2, questionId++);
                insertQStmt.setString(3, question.name);
                insertQStmt.setString(4, question.desc);
                insertQStmt.setInt(5, question.points);
                insertQStmt.executeUpdate();
            }
        }
        return exercise.id;
    }


    /**
     * Return a list of all the exercises in the database.
     * <p>
     * The list should be sorted by exercise id.
     *
     * @return list of all exercises.
     * @throws SQLException
     */
    public List<Exercise> loadExercises() throws SQLException {
        //Step 1: Get all Exercises
        String getEx = "SELECT ExerciseId, Name, DueDate FROM Exercise ORDER BY ExerciseId";
        List<Exercise> exercises = new ArrayList<>();
        try (PreparedStatement selectStmt = db.prepareStatement(getEx)) {
            ResultSet result = selectStmt.executeQuery();
            //Step 2: Create Exercise object
            while (result.next()) {
                int exId = result.getInt("ExerciseId");
                String name = result.getString("Name");
                Date dueDate = new Date(result.getLong("DueDate"));
                Exercise Nexercise = new Exercise(exId, name, dueDate);
                //Step 3: Add Questions
                String getQ = "SELECT Name, Desc, Points FROM Question WHERE ExerciseId = ?";
                try (PreparedStatement selectQStmt = db.prepareStatement(getQ)) {
                    selectQStmt.setInt(1, exId);
                    ResultSet resultQ = selectQStmt.executeQuery();
                    while (resultQ.next()) {
                        String nameQ = resultQ.getString("Name");
                        String descQ = resultQ.getString("Desc");
                        int pointsQ = resultQ.getInt("Points");
                        Nexercise.addQuestion(nameQ, descQ, pointsQ);
                    }
                }
                //Step 4: Add Exercise to list
                exercises.add(Nexercise);
            }
        }
        return exercises;
    }

    // ========== Submission Storage ===============

    /**
     * Store a submission in the database.
     * The id field of the submission will be ignored if it is -1.
     * <p>
     * Return -1 if the corresponding user doesn't exist in the database.
     *
     * @param submission
     * @return the submission id.
     * @throws SQLException
     */
    public int storeSubmission(Submission submission) throws SQLException {
        // Step 1: Check if the user exists
        String check = "SELECT Username FROM User WHERE Username = ?";
        try (PreparedStatement checkStmt = db.prepareStatement(check)) {
            checkStmt.setString(1, submission.user.username);
            try (ResultSet result = checkStmt.executeQuery()) {
                if (!result.next()) {
                    // User doesn't exist
                    return -1;
                }
            }
        }
        // Step 2: Store the submission
        String storeSubmission;
        if (submission.id == -1) {
            storeSubmission = "INSERT INTO Submission (UserId, ExerciseId, SubmissionTime) VALUES ((SELECT UserId FROM User WHERE Username = ?), ?, ?)";
        } else {
            storeSubmission = "INSERT INTO Submission (SubmissionId, UserId, ExerciseId, SubmissionTime) VALUES (?, (SELECT UserId FROM User WHERE Username = ?), ?, ?)";
        }
        int subID;
        try (PreparedStatement storeStmt = db.prepareStatement(storeSubmission)) {
            int index = 1;
            if (submission.id != -1) {
                storeStmt.setInt(index++, submission.id);
            }
            storeStmt.setString(index++, submission.user.username);
            storeStmt.setInt(index++, submission.exercise.id);
            storeStmt.setLong(index, submission.submissionTime.getTime());
            storeStmt.executeUpdate();
            ResultSet keys = storeStmt.getGeneratedKeys();
            if (keys.next()) {
                subID = keys.getInt(1);
            } else {
                if (submission.id != -1) {
                    subID = submission.id;
                } else {
                    subID = -1;
                }
            }
        }
        // Step 3: Insert the question grades
        String QGrad = "INSERT INTO QuestionGrade (SubmissionId, QuestionId, Grade) VALUES (?, ?, ?)";
        try (PreparedStatement QGradStmt = db.prepareStatement(QGrad)) {
            for (int i = 0; i < submission.questionGrades.length; i++) {
                QGradStmt.setInt(1, submission.id);
                QGradStmt.setInt(2, (i+1));
                QGradStmt.setFloat(3, submission.questionGrades[i]);
                QGradStmt.executeUpdate();
            }
        }
        return subID;
    }


    // ============= Submission Query ===============


    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the latest submission for the given exercise by the given user.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getLastSubmission(User, Exercise)}
     *
     * @return
     */
    PreparedStatement getLastSubmissionGradesStatement() throws SQLException {
        String query =
                "WITH LatestSubmission AS ( " +  //We temporary save the Submission id and the Submission time.
                        "    SELECT SubmissionId, MAX(SubmissionTime)" +
                        "    FROM Submission " +
                        "    WHERE UserId = (SELECT UserId FROM User WHERE Username = ?) " + //User's username
                        "      AND ExerciseId = ? " + //Exercise Id
                        ") " +
                        "SELECT Submission.SubmissionId, QuestionGrade.QuestionId, QuestionGrade.Grade, Submission.SubmissionTime " + //Columns of new table
                        "FROM Submission " +
                        "JOIN QuestionGrade ON Submission.SubmissionId = QuestionGrade.SubmissionId " + //Join tables using SubmissionId
                        "WHERE Submission.SubmissionId = (SELECT SubmissionId FROM LatestSubmission) " + //Include only rows that match the SubmissionId we found earlier.
                        "ORDER BY QuestionGrade.QuestionId " +
                        "LIMIT ?"; //number of questions in the given exercise

        return db.prepareStatement(query);
    }

    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the <i>best</i> submission for the given exercise by the given user.
     * The best submission is the one whose point total is maximal.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getBestSubmission(User, Exercise)}
     *
     */
    PreparedStatement getBestSubmissionGradesStatement() throws SQLException {
        // TODO: Implement
        return null;
    }

    /**
     * Return a submission for the given exercise by the given user that satisfies
     * some condition (as defined by an SQL prepared statement).
     * <p>
     * The prepared statement should accept the user name as parameter 1, the exercise id as parameter 2 and a limit on the
     * number of rows returned as parameter 3, and return a row for each question corresponding to the submission, sorted by questionId.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @param stmt
     * @return
     * @throws SQLException
     */
    Submission getSubmission(User user, Exercise exercise, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, user.username);
        stmt.setInt(2, exercise.id);
        stmt.setInt(3, exercise.questions.size());

        ResultSet res = stmt.executeQuery();

        boolean hasNext = res.next();
        if (!hasNext)
            return null;

        int sid = res.getInt("SubmissionId");
        Date submissionTime = new Date(res.getLong("SubmissionTime"));

        float[] grades = new float[exercise.questions.size()];

        for (int i = 0; hasNext; ++i, hasNext = res.next()) {
            grades[i] = res.getFloat("Grade");
        }

        return new Submission(sid, user, exercise, submissionTime, (float[]) grades);
    }

    /**
     * Return the latest submission for the given exercise by the given user.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @return
     * @throws SQLException
     */
    public Submission getLastSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getLastSubmissionGradesStatement());
    }


    /**
     * Return the submission with the highest total grade
     *
     * @param user the user for which we retrieve the best submission
     * @param exercise the exercise for which we retrieve the best submission
     * @return
     * @throws SQLException
     */
    public Submission getBestSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getBestSubmissionGradesStatement());
    }
    
}


