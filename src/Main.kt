//TIP This program is not designed to be user-friendly. It is designed as a base for an android app I intend to create.
// I have put in minimal effort to make the app flow smoothly.
import java.io.File
import java.time.LocalDateTime
import java.time.Duration

fun main() {
    // Creating a User object to use throughout the program
    val user = User()
    // I'll start with the user selection menu to log in the user.
    // See menuLoop function for the main logic running the application
    mainLoop(UserSelectMenu(user))
}

/**
 * The main logic of the application. Takes a [Menu] object. Loops over the two main functions,
 * display() and handleInput() to first display the menu's contents to the user and then
 * decide what actions to take based on the input from the user.
 *
 * @param startingMenu The first menu shown to the user. Currently, the user selection menu
 */
fun mainLoop(startingMenu: Menu) {
    // Define a nullable Menu variable that will be changed throughout the program initialized with the first menu.
    var currentMenu: Menu? = startingMenu

    // This is the main logic of the application. A menu's display method is called to show the menu.
    // The user provides input to select menu items. Then the handleInput method processes the response
    // and returns the next menu to display. This will loop until a null value is returned.
    while (currentMenu != null) {
        currentMenu.display()
        val input = readLine()?.toIntOrNull()
        currentMenu = currentMenu.handleInput(input)
    }
}

/**
 * The User object is passed to each menu for display and to help
 * each function know who's logged in.
 *
 * @property id The user id. Set automatically by the application
 * @property firstName The first name of the user
 * @property lastName The last name of the user
 */
data class User(
    var id: Int? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var clockedInStatus: String = "out",
    var lastTimePunch: LocalDateTime? = null
)

/**
 * The project object is used to help menus that process them
 * remember which project has been selected.
 *
 * @property id The project id. Set automatically by the application
 * @property name The name or title of the project
 * @property description A short description of the project
 */
data class Project(
    var id: Int? = null,
    var name: String? = null,
    var description: String? = null,
    var totalTime: Int = 0
)

/**
 * Used to help the program process time punches.
 *
 * @property id The time card id. Set automatically by the application
 * @property userId This id will match an id in the users.csv file
 * @property projectId This id will match an id in the project.csv file
 * @property timePunch The time this punch was set
 * @property description When clocking in the user can create a description. When clocking out it is
 * set to CLOCKING OUT
 */
data class TimeCard(
    var id: Int?,
    var userId: Int?,
    var projectId: Int?,
    var timePunch: LocalDateTime?,
    var description: String?
)

/**
 * Used for the main logic of the application. Helps the main loop
 * know which functions to run to display menus and process input.
 *
 * @property display The method holding the menu's printing statements
 * @property handleInput The method that processes user input and returns a Menu object
 */
interface Menu{
    /**
     * Contains the menu's print statements that show the user the menu's content
     */
    fun display()

    /**
     * Hold's the when expression that decides what to do based on a user's input.
     *
     * @param input The user input which will be either a number or null
     * @return [Menu] or null
     */
    fun handleInput(input: Int?): Menu?
}

/**
 * Used to display a greeting on almost every page. Made into a function for easy change.
 *
 * @param user Used to display the user's name.
 */
fun displayGreeting(user: User) {
    println("Hello ${user.firstName}!")
}

/**
 * Creates a list of User objects from the data found in users.csv
 *
 * @return List of user objects
 */
fun getUserList(): List<User> {
    val reader = File("src/csv/users.csv").bufferedReader()
    reader.readLine()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (id, firstName, lastName, clockedInStatus, lastTimePunch) = it.split('|', ignoreCase = false, limit=5)
            val getLastTimePunch = if (lastTimePunch == "null") {null} else {LocalDateTime.parse(lastTimePunch.trim())}
            User(id.trim().toInt(), firstName.trim(), lastName.trim(), clockedInStatus.trim(), getLastTimePunch)
        }.toList()
}

/**
 * Takes a list of User objects and writes their data into users.csv
 *
 * @param [users] A list of User objects
 */
fun writeUserCsv(users: List<User>) {
    val file = File("src/csv/users.csv")
    file.writeText("Id|FirstName|LastName|ClockedInStatus|LastTimePunch")
    for (user in users) {
        file.appendText("\n${user.id}|${user.firstName}|${user.lastName}|${user.clockedInStatus}|${user.lastTimePunch}")
    }
}

/**
 * Generates a new list of User objects that can be written to users.csv to update information about a certain user.
 *
 * @param user A [User] object that has been modified by the user. Will be used to replace the [User] with the same
 * id with the [User] with new data.
 */
fun modifyUserCsv(user: User) {
    val userList = getUserList()
    val newUserList = userList.map { userItem ->
        if (userItem.id == user.id) user
        else userItem
    }
    writeUserCsv(newUserList)
} // TODO: Use this function when creating the user settings menu

/**
 * Creates a list of [Project] objects from the data found in projects.csv
 *
 * @return List of [Project] objects
 */
fun getProjectList(): List<Project> {
    val reader = File("src/csv/projects.csv").bufferedReader()
    reader.readLine()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (id, name, description, totalTime) = it.split('|', ignoreCase = false, limit=4)
            Project(id.trim().toInt(), name.trim(), description.trim(), totalTime.trim().toInt())
        }.toList()
}

/**
 * Takes a list of [Project] objects and writes their data into project.csv
 *
 * @param projects A list of [Project] objects
 */
fun writeProjectCsv(projects: List<Project>) {
    val file = File("src/csv/projects.csv")
    file.writeText("Id|Name|Description|TotalTime")
    for (project in projects) {
        file.appendText("\n${project.id}|${project.name}|${project.description}|${project.totalTime}")
    }
}

/**
 * Generates a new list of [Project] objects that can be written to projects.csv to update information about a certain project.
 *
 * @param project A [Project] object that has been modified by the user. Will be used to replace the [Project] from the csv
 * file with the same id as the [project] parameter.
 */
fun modifyProjectCsv(project: Project?) {
    if (project != null) {
        val projectList = getProjectList()
        val newProjectList = projectList.map { projectItem ->
            if (projectItem.id == project.id) project
            else projectItem
        }
        writeProjectCsv(newProjectList)
    }
} // TODO: modify the modify project menu to utilize this function

/**
 * @param projectId Project id for the desired project
 *
 * @return a single [Project] from the projects csv file that has a matching [projectId]
 */
fun getProject(projectId: Int): Project? {
    val projectList = getProjectList()
    return projectList.find { it.id == projectId }
}

/**
 * Creates a list of [TimeCard] objects from the data found in timeCard.csv
 *
 * @return List of [TimeCard] objects
 */
fun getTimeCardList(): List<TimeCard> {
    val reader = File("src/csv/timeCard.csv").bufferedReader()
    reader.readLine()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (id, userId, projectId, timePunch, description) = it.split('|', ignoreCase = false, limit=5)
            TimeCard(id.trim().toInt(), userId.trim().toInt(), projectId.trim().toInt(), LocalDateTime.parse(timePunch.trim()), description.trim())
        }.toList()
}

/**
 * Takes a list of [TimeCard] objects and writes their data into timeCard.csv
 *
 * @param timeCards A list of [TimeCard] objects that will be written to the csv file
 */
fun writeTimeCardCsv(timeCards: List<TimeCard>) {
    val file = File("src/csv/timeCard.csv")
    file.writeText("Id|UserId|ProjectId|TimePunch|Description")
    for (timeCard in timeCards) {
        file.appendText("\n${timeCard.id}|${timeCard.userId}|${timeCard.projectId}|${timeCard.timePunch}|${timeCard.description}")
    }
}

// TODO: Using TODO to set a mark for the MainMenu page
// TODO
/**
 * The main menu of the application. This menu leads to all others. Requires a [User] to be selected first.
 * See the menu options below in [display].
 */
class MainMenu(private val user: User): Menu {
    override fun display() {
        displayGreeting(user)
        println("""
            Please enter the number for the menu item you would like to select:
            
            1. Clock in/out
            2. Get time reports
            3. Edit projects
            4. Settings
            5. Switch user
            
            0. Exit
            
        """.trimIndent())
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return ClockInOutMenu(user)
            2 -> println("GO TO GET TIME REPORTS")
            3 -> return ProjectListMenu(user)
            4 -> println("GO TO SETTINGS")
            5 -> return UserSelectMenu(user)
            0 -> return null
            else -> println("Selection not in menu, enter new item.")
        }
        return MainMenu(user)
    }
}

/*
* The following classes contain the data for menus related to
* creating new users and switching which user is logged in.
*/

/**
 * The starting menu of the application. If there is no [User] selected a [User] must be chosen from this
 * menu or the application should be closed. If there is a [User] the option to exit the application is
 * replaced with the option to return to the [MainMenu].
 */
class UserSelectMenu(private val user: User): Menu {
    override fun display() {
        if (user.id != null) println("Current user: ${user.firstName}\n")
        println("""
        Please select a user below or create a new user:
        
        1. Create new user
        """.trimIndent())

        val userList = getUserList()

        for ((index, user) in userList.withIndex()) {
            println("${index + 2}. ${user.firstName} ${user.lastName}")
        }

        if (user.id != null) {
            println("\n0. Cancel and return to previous menu")
        } else println("\n0. Exit")
    }

    override fun handleInput(input: Int?): Menu? {
        val userList = getUserList()
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return CreateUserMenu(user)
            in 2..userList.size + 1 -> {
                val newUser = userList[input - 2]
                user.id = newUser.id
                user.firstName = newUser.firstName
                user.lastName = newUser.lastName
                user.clockedInStatus = newUser.clockedInStatus
                user.lastTimePunch = newUser.lastTimePunch
                return MainMenu(user)
            }
            0 -> {
                return if (user.id != null) MainMenu(user)
                else null
            }
            else -> println("Selection not in menu, enter new item.")
        }

        return UserSelectMenu(user)
    }
}

/**
 * Allows a user to input the first and last name of a new user. It then gives the user three
 * options. First, they can save the user data and return to the [MainMenu]. Second, they can discard
 * and re-enter new user data. Third, they can discard the new data and return to the [UserSelectMenu].
 *
 * @property createUser Function used to create a new user and write it to the users csv file.
 */
class CreateUserMenu(private val user: User, private var firstName: String? = null, private var lastName: String? = null): Menu {
    override fun display() {
        if (firstName == null) {
            println("""
            Enter new user data
            
            """.trimIndent())

            print("First name: ")
            firstName = readLine()
            print("Last name: ")
            lastName = readLine()
        }

        println("""
        
        Name entered: $firstName $lastName
        Please enter the number of the appropriate option below
        
        1. Save new user
        2. Discard and create new user
        
        0. Discard and return to user selection menu
        """.trimIndent())
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return createUser(user, firstName, lastName)
            2 -> return CreateUserMenu(user)
            0 -> return UserSelectMenu(user)
            else -> println("Selection not in menu, enter new item.")
        }
        return(CreateUserMenu(user, firstName, lastName))
    }


    fun createUser(user: User, firstName: String?, lastName: String?): Menu {
        user.firstName = firstName
        user.lastName = lastName
        user.clockedInStatus = "out"
        user.lastTimePunch = null
        val userList = getUserList()
        val lastId = userList.lastOrNull()?.id
        if (lastId == null) user.id = 1
        else user.id = lastId + 1
        val newUserList = userList + user
        writeUserCsv(newUserList)
        return MainMenu(user)
    }
} //TODO: edit this function to include setting the user.lastTimePunch property so it doesn't roll over from any current user.

/*
* The following classes hold the data for project creation, modification, and deletion.
*/

/**
 * Displays options to create a new project and a list of all projects found in the project csv file. Once a project is selected
 * the [ProjectDetailsMenu] is shown. If the user chooses to create a new project they are taken to the [CreateProjectMenu].
 * Otherwise, users are sent to the [MainMenu]
 */
class ProjectListMenu(private val user: User): Menu {
    private val projectList: List<Project> = getProjectList()

    override fun display() {
        displayGreeting(user)
        println("""
            Please select a project below or create a new one
            
            1. Create new project
        """.trimIndent())

        for ((index, project) in projectList.withIndex()) {
            println("${index + 2}. ${project.name}")
        }

        println("\n0. Return to previous menu")
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return CreateProjectMenu(user)
            in 2..projectList.size + 1 -> {
                return ProjectDetailsMenu(user, projectList[input - 2])
            }
            0 -> return MainMenu(user)
            else -> println("Selection not in menu, enter new item.")
        }
        return ProjectListMenu(user)
    }
}

/**
 * Allows a user to input the name and description name of a new project. It then gives the user three
 * options. First, they can save the project data and return to the [MainMenu]. Second, they can discard
 * and re-enter new project data. Third, they can discard the new data and return to the [ProjectListMenu].
 *
 * @property createProject Function used to create a new project and write it to the projects csv file.
 */
class CreateProjectMenu(private val user: User, private var name: String? = null, private var description: String? = null): Menu {
    override fun display() {
        displayGreeting(user)
        if (name == null) {
            print("""
                
                Enter new project data
                
                Project name: 
            """.trimIndent())
            name = readLine()
            println("""
            Project description
            Must not include the "|" symbol
            """.trimIndent())
            print("> ")
            description = readLine()
        }

        println("""
        
        Project entered:
        Name: $name
        Description: $description
        
        Please enter the number of the appropriate option below
        
        1. Save new project
        2. Discard and create new project
        
        0. Discard and return to project selection menu
        """.trimIndent())
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return createProject()
            2 -> return CreateProjectMenu(user)
            0 -> return ProjectListMenu(user)
            else -> println("Selection not in menu, enter new item.")
        }
        return CreateProjectMenu(user, name, description)
    }

    fun createProject(): Menu {
        // TODO: I'll have to figure out how to check the string for null or '|' later
//        if (name.isNullOrEmpty() || description.isNullOrEmpty() || '|' in name || description.contains('|')) {
//            println("!!! Name or description was left blank or included the '|' character. Please create a new project. !!!")
//            return createProjectMenu(user, name, description)
//        }
        var projectId = 1
        val projectList = getProjectList()
        val lastId = projectList.lastOrNull()?.id
        if (lastId != null) projectId = lastId + 1
        val newProject = Project(projectId, name, description)
        val newProjectList = projectList + newProject
        writeProjectCsv(newProjectList)
        return ProjectListMenu(user)
    }

}

/**
 * Shows the user the name and description of the project as well as the total time spent on the project.
 * Gives the user three options. First, to edit the details and show them the [ModifyProjectMenu]. Second,
 * to delete the project, showing them the [ProjectDeletionConfirmation] menu. Third, to return the
 * [ProjectListMenu].
 */
class ProjectDetailsMenu(private val user: User, private val project: Project): Menu {
    override fun display() {
        displayGreeting(user)
        println("""
            Project selected:
            ${project.name}
            ${project.description}
            Total time spent on project: ${(project.totalTime / 60)} hours ${(project.totalTime % 60)} minutes
            
            Please enter the number of the appropriate option below
            
            1. Edit project details
            2. Delete project
            
            0. Return to project selection menu
        """.trimIndent())
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return ModifyProjectMenu(user, project)
            2 -> return ProjectDeletionConfirmation(user, project)
            0 -> return ProjectListMenu(user)
            else -> println("Selection not in menu, enter new item.")
        }
        return ProjectDetailsMenu(user, project)
    }

}

/**
 * Shows the current project name and description. Gives the user three options. First, to edit the project name.
 * Second, to edit the project description. Third, to cancel and return to [ProjectDetailsMenu]. Both edit options
 * will also return the user to the [ProjectDetailsMenu].
 *
 * @property editName Gives the user a prompt to give a new name for the project. If the prompt is left blank
 * it will cancel the edit operation and return to [ProjectDetailsMenu]
 * @property editDescription Gives the user a prompt to give a new description for the project. If the prompt is left blank
 * it will cancel the edit operation and return to [ProjectDetailsMenu]
 */
class ModifyProjectMenu(private val user: User, private val project: Project): Menu {
    override fun display() {
        displayGreeting(user)
        println("""
            
            Project name: ${project.name}
            Description: ${project.description}
            
            Which part would you like to modify:
            
            1. Project name
            2. Project Description
            
            0. Cancel and return to project details
        """.trimIndent())
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return editName()
            2 -> return editDescription()
            0 -> return ProjectDetailsMenu(user, project)
            else -> println("Selection not in menu, enter new item.")
        }
        return ModifyProjectMenu(user, project)
    }

    fun editName(): Menu {
        print("""
            
            Enter new project name
            Leave blank to cancel
            > 
        """.trimIndent())

        val name = readLine()

        if (!name.isNullOrEmpty()) {
            project.name = name
            val projectList = getProjectList()
            val newProjectList = projectList.map { projectItem ->
                if (projectItem.id == project.id) project
                else projectItem
            }
            writeProjectCsv(newProjectList)
        }
        return ProjectDetailsMenu(user, project)
    }

    fun editDescription(): Menu {
        print("""
            
            Enter new description
            Leave blank to cancel
            > 
            """.trimIndent())

        val description = readLine()

        if (!description.isNullOrEmpty()) {
            project.description = description
            val projectList = getProjectList()
            val newProjectList = projectList.map { projectItem ->
                if (projectItem.id == project.id) project
                else projectItem
            }
            writeProjectCsv(newProjectList)
        }
        return ProjectDetailsMenu(user, project)
    }
}

/**
 * Gives the user two options. First, to confirm the deletion of the shown project. Second, to cancel.
 * Both options return the user to the [ProjectDetailsMenu].
 *
 * @property deleteProject Removes the selected project from the projects csv file.
 * * WARNING: CANNOT BE UNDONE
 */
class ProjectDeletionConfirmation(private val user: User, private val project: Project): Menu {
    override fun display() {
        displayGreeting(user)
        println("""
            
            Are you sure you would like to delete ${project.name}?
            
            1. Confirm
            
            0. Cancel and return to project details
        """.trimIndent())
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            1 -> return deleteProject()
            0 -> return ProjectDetailsMenu(user, project)
            else -> println("Selection not in menu, enter new item.")
        }
        return ProjectDeletionConfirmation(user, project)
    }

    fun deleteProject(): Menu {
        val projectList = getProjectList()
        val newProjectList = projectList.filterNot { it.id == project.id }
        writeProjectCsv(newProjectList)
        return(ProjectListMenu(user))
    }
}

/*
 * The following menus are used to clock in and out the user from projects.
 */

/**
 * Tells the user whether they are clocked in or out and for how long. Gives different options
 * depending on if they are clocked in or out. If clocked out, allows the user to clock in, showing
 * them the [ClockInSelectionMenu]. If clocked in, allows the user to clock out and return to [MainMenu],
 * or switch projects which will clock them out of the current project and show them the [ClockInSelectionMenu].
 * In either case, it will allow the user to edit the time card (NOT IMPLEMENTED) or cancel and return to [MainMenu].
 *
 * @property clockOut Creates a new entry in the timeCard csv file and adjusts the project total time as well
 * as the users clocked in status and last time punch.
 */
class ClockInOutMenu(private val user: User): Menu {
    val isClockedIn = user.clockedInStatus != "out"
    override fun display() {
        displayGreeting(user)
        if (isClockedIn) print("CLOCKED IN - ")
        else print("CLOCKED OUT - ")
        if (user.lastTimePunch == null) {
            println("Never!")
        } else {
            val timeSinceLastPunch = Duration.between(user.lastTimePunch, LocalDateTime.now()).toMinutes()
            println("${timeSinceLastPunch / 60} hours and ${timeSinceLastPunch % 60} minutes ago")
        }
        println("What would you like to do?")
        if (isClockedIn) {
            println("""
                    1. Clock out
                    2. Switch projects
                    3. Edit time card
                    """.trimIndent())
        } else {
            println("""
                    1. Clock in
                    2. Edit time card
                    """.trimIndent())
        }
        println("\n0. Cancel and return to main menu")
    }

    override fun handleInput(input: Int?): Menu? {
        if (isClockedIn) {
            when (input) {
                null -> println("No selection made, please enter an item number.")
                1 -> return clockOut()
                2 -> return ClockInSelectionMenu(user)
                3 -> println("EDIT TIME CARD\nTo edit the time card, please add, edit, or remove lines from the csv for now.")
                0 -> return MainMenu(user)
                else -> println("Selection not in menu, enter new item.")
            }
        } else {
            when (input) {
                null -> println("No selection made, please enter an item number.")
                1 -> return ClockInSelectionMenu(user)
                2 -> println("EDIT TIME CARD\nTo edit the time card, please add, edit, or remove lines from the csv for now.")
                0 -> return MainMenu(user)
                else -> println("Selection not in menu, enter new item.")
            }
        }
        return ClockInOutMenu(user)
    }

    fun clockOut(): Menu {
        val currentTime = LocalDateTime.now()
        val timeCardList = getTimeCardList()
        val lastId = timeCardList.lastOrNull()?.id
        var timeCardId = 1
        if (lastId != null) timeCardId = lastId + 1
        val clockOutTimeCard = TimeCard(timeCardId, user.id, user.clockedInStatus.toInt(), currentTime, "CLOCKING OUT")
        val newTimeCardList = timeCardList + clockOutTimeCard
        writeTimeCardCsv(newTimeCardList)
        // TODO: get project from id, set number of hours spent to new value, modify the project csv with new project object
        val project = getProject(user.clockedInStatus.toInt())
        val minutesSinceClockIn = Duration.between(user.lastTimePunch, LocalDateTime.now()).toMinutes().toInt()
        project?.totalTime += minutesSinceClockIn
        modifyProjectCsv(project)
        user.clockedInStatus = "out"
        user.lastTimePunch = currentTime
        modifyUserCsv(user)
        return MainMenu(user)
    }
}

/**
 * Shows as options a list of all projects. When one is selected, [clockIn] is called to clock in the user to that project.
 * They are also given the option to cancel and return to the [ClockInOutMenu].
 *
 * @property clockIn If the user is already clocked in it creates a new entry in the timeCard csv file to clock out
 * the user before creating a second entry to clock them in. If the user is clocked out, it simply adds one entry to clock
 * in the user.
 */
class ClockInSelectionMenu(private val user: User, private val projectList: List<Project> = getProjectList()): Menu {
    private val isClockedIn = user.clockedInStatus != "out"

    override fun display() {
        displayGreeting(user)
        println("Please select a project to work on below:\n")

        for ((index, project) in projectList.withIndex()) {
            println("${index + 1}. ${project.name}")
        }

        println("\n0. Cancel and return to previous menu")
    }

    override fun handleInput(input: Int?): Menu? {
        when (input) {
            null -> println("No selection made, please enter an item number.")
            in 1..projectList.size -> {
                return clockIn(projectList[input - 1])
            }
            0 -> return ClockInOutMenu(user)
            else -> println("Selection not in menu, enter new item.")
        }
        return ClockInSelectionMenu(user, projectList)
    }

    fun clockIn(project: Project): Menu {
        val currentTime = LocalDateTime.now()
        if (isClockedIn) {
            val timeCardList = getTimeCardList()
            val lastId = timeCardList.lastOrNull()?.id
            var timeCardId = 1
            if (lastId != null) timeCardId = lastId + 1
            val clockOutTimeCard = TimeCard(timeCardId, user.id, user.clockedInStatus.toInt(), currentTime, "CLOCKING OUT")
            val newTimeCardList = timeCardList + clockOutTimeCard
            writeTimeCardCsv(newTimeCardList)
            val project = getProject(user.clockedInStatus.toInt())
            val minutesSinceClockIn = Duration.between(user.lastTimePunch, LocalDateTime.now()).toMinutes().toInt()
            project?.totalTime += minutesSinceClockIn
            modifyProjectCsv(project)
        }
        print("""
            Description of session:
            > 
        """.trimIndent())
        val description = readLine()
        val timeCardList = getTimeCardList()
        val lastId = timeCardList.lastOrNull()?.id
        var timeCardId = 1
        if (lastId != null) timeCardId = lastId + 1
        val clockInTimeCard = TimeCard(timeCardId, user.id, project.id, currentTime.plusSeconds(1), description)
        val newTimeCardList = timeCardList + clockInTimeCard
        writeTimeCardCsv(newTimeCardList)
        user.clockedInStatus = project.id.toString()
        user.lastTimePunch = currentTime.plusSeconds(1)
        modifyUserCsv(user)
        return MainMenu(user)
    }
}