@@ -1,55 +1 @@
**TIC TAC TOE GAME**

**a**)

Sandeep Rout, 2020A7PS1711G, f20201711@goa.bits-pilani.ac.in

**b**)

The app is a classic Tic Tac Toe game also known as O and X a two player game. One of the player chooses X and the other player plays with X or vice versa. The rule of the game is simple- Either of the player is able to mark three X or O depending upon the piece type chosen by them in a straight row or column or in a diagonal way first wins the game, if none of the player is able to fulfil the above condition the game is drawn. The bugs found in the app is as follows- The game logic is not very smart on the computer part as the computer picks up any empty cell in the 3x3 matric without checking for the opponent wining chances.

**c**)

**1)**The login fragment of the app asks user to signin/register with Email and Password supported by FirebaseAuth, Firebase is a google backed NoSQL and multifunctional Database platform. FirebaseAuth is firebase Service provider for Authentication of the users with their Email and Password the login fragment initially checks the availabilty of the user in the registered users list, if the user is already registed then they are directed to the DashBoard Fragment. If not the user is first registered with the new email and Password extracted from the Edit text Fields. On Login/Register button click the user is allowed to navigate to Dash Board Fragement.I have implemented the "logout" button in the menu bar when clicked upon the user is brought to the login page again.

The textviews of wins, losses and draws are visible on the dashboard displaying the user's history of number of games won, lost and drawn. The data is synced with the firebase realtime database which contains the child database history of each registered player. I have implemented a recycler view with a OpenGameSAdapter containing the list of active games hosted by different players waiting for another player to join their lobby.

The floating Action Button in the bottom right section of the screen allows the user to play or create a new game. A alert Dialog Box pops up whenever the floating Action is clicked upon which asks the user to choose from One-Player(vs Computer) or Two-Player Game. If the user selects One-Player Game a 3x3 grid of buttons is displayed for the game to begin played against the computer. If the user presses Two-Player option a new game will be created in the lobby and same will be reflected in the Dashboard for a new player to select from and start playing together synced by Firebase's realtime Database.

**2)** 

**a)** The user first chooses from the available options of 'O' and 'X' to begin the game with.

**b)** When the user clicks on a particular cell that cell is disabled permanently, after that user waits for computer to make its move which is quick and random , based on that the user can make their move. If the user wins a "Congratulations!!" message is shown with a updated score visible on the Dashboard, similarly the Loss count is updated upon losing. Clicking “OK” on it takes the user back to the dashboard

**c**) If the user loses the game the app immediately pops up the message "Sorry!!" and takes the user back to the Dashboard when clicked upon OK,

**d**) If no one wins the draw count is incremented in the dashboard.

**3)**

**a)**

When the user has clicked upon the create option of the Two-Player option in the alert Dialog Box which shows when the floating Action Button is clicked upon, they are brought to the GameFragment where they wait for a new player to join the game, till then the Progress Dialog Box spins. Once the host has created the game, a lobby will be created in the Firebase realtime Database which will then update the recycle view enabling new players to choose from the available games to choose from. When a new player selects from the available game , the two players are at once shifted to a running game database with a board created. Each will wait for their respective turns to make and the game proceeds till one of them win or they both draw.

**b)**

The app waits for the turn of each player and displays the respective dialogs the user when they win, lose or draw. The buttons are disabled in both of their boards when anyone of the player clicks upon the button out the available 3x3 matrix of cells.

**c)**

If the user forfeits the game in between their loss count is incremented and they are brought to the Dashboard.

**d**

Whenever the host clicks upon the create option a seperate database is created in the realtime database with the name of lobbies which have gameID as the child and  the name of the player as the child. A progress Dialog box keeps on spinning on till a new players joins the same lobby, which will then bring the user to the Game Fragment along the gameID of the active game, once matched with the gameID, the progress Dialog box will be dismissed and the game wil begin between the players. Internally in Firebase the active game will be removed from the lobbies database and both the players will join a new Database called Running Game with a board created sycned asynchronously between the two players, their each move will be reflected in their respective boards. If anyone of them win or lose they will brought back to the Dashboard with updated scores.

**e**

**f**

It took me more than 30 hours to complete it

 **g**

 Difficulty:10/10
