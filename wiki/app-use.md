# App use guide

## Creating account

- In order to use the app, an account needs to be created.
- At the moment, only email and password, plus google SSO are available.
- But really, email and password doesn't really work. You have to create the user authentication on firebase.
- Also, for email and password, user document is not created on first login in firestore, plus there are more issues that are resolved for google sign in.
- Eventually, this needs to be fixed, plus giving the ability to the user to create their own email and password account (email or text verification).
- Also forgotten password feature
- Consider other sign in methods, not just email and password, and google.
- The user can also log out, obviously

## User profile

- Users will be able to see their information
- At the moment, when signing in with google, display name and profile picture are retrieved from the google account and saved on the user document on firestore.
- Eventually, users will be able to modify their display name and profile picture.
- They will probably be able to do something else on their profile or get some stats, this is to be defined.
- At the moment, the date the account was created appears in the profile. That needs changing because that would go under settings - Account. One thing is profile, different thing is account.
- Also in settings, eventually, they will be able to manage their subscriptions, account status, etc.

## Creating groups

After the user creates their account, the next logical step is to create a group. The app is all about shared expenses while traveling with all types of groups.

The main focus is while traveling because the app focus is offline-first and multiple currency (with exchange rates, etc). But really, people could use this app for anything they like.

The first issue that comes to mind is that the app must handle all types of groups, even if it's just one person. A solo traveler needs to create a group if they want to track their expenses, so let's say a group is the minimal entity needed to actually do something in the app.

Types of groups - real life scenarios

- Solo traveler: Juan
- Couple: Juan and Alberto
- Family: Sarah, John, Connor
- Friends: Juan, Alberto, María, Pepa, Laura, Inés, Mario
- Friends with kids: Juan, Alberto, María, Pepa, Laura, Inés, Mario, John, Connor
- Work trip: Mike, James, Kirstyn, Alberto, Gonzalo

So essentially, a group can be created with a name, an optional description (eg to save what it is for), a list of people (members) that will participate. An optional image (yet to be implemented).
And yes, a default currency. Why currency, well... the app is to track expenses, so all members in a group must agree to use one currency by default.
All amounts in the group, even in other currencies, will be exchanged (visually) to the default group currency at all times so everybody has visibility of how much money it is.

Also, when creating a group, an optional list of currencies may be specified so that they're available to select when logging expenses, additions, substractions. Cash flow in the end.

At the moment, you can only add members to the group at the time of creation, by their exact email.
Eventually, some other ways of adding members will be available. Even after group creation, invitation, etc.

At the moment, a group cannot be modified.

At the moment, a group can be deleted without any confirmation and by any member. This needs changing when roles are implemented.
I guess each group member will have a role eventually.

The more you think, the more possibilities there are. Bear in mind that we're not even talking about money yet.
First, we need to know how those large groups are divided into subunits.

From now on, and forgetting about user profile and settings, every single operation the user can do is tied to a group. So everything else is only available in the app if a group is selected.

User sees a list of groups (they've created or someone else has created and included them).

## Subunits

The concept of subunits comes when the group is a group of groups.

Eg, 4 friends that are 2 couples. Each couple manages their money differently, they could contribute for the two of them, they could agree to pay differently eg instead of 50/50, 40/60.
Members in a group that are a couple with a child of 1 year could agree that the child doesn't pay for anything, so shares could be 50/50/0, or maybe if the child is 6, they could agree for them to pay just 15%, so the shares would be 40/45/15, etc.

So basically, they serve the purpose of grouping people within a group, and also defining shares among them.

- At the moment, any user can create a subunit within a group, give a name, and select members and assign shares.
- A member in a group can only belong to one subunit.
- A member can be in a group and not belong to any subunit.

At the moment, but may change eventually
- A subunit can be modified by any group member
- A subunit can be removed by any group member.

The following are just examples of subunits:

Group: Trip to Thailand
members: 9

Andrés
Antonio
Jose
Mary
Irene
Javi
Gonzalo
Inca
David

subunits: 4

subunit-1: Sevillanos (Andrés and Antonio, 50/50)
subunit-2: Cordobeses (Jose and Mary, 60/40)
subunit-3: Gaditanos (Javi and Irene, 50/50)
subunit-4: Soltero (Gonzalo, 100)

While Gonzalo chose to create a subunit for himself, Inca and David, also solo travelers chose not to, and that's allowed.

Also, Sevillanos and Gaditanos chose to share their money equally, while Cordobeses chose to split differently.

Another example:

Group: Trip to Mexico
members: 4

Andrés
Antonio
Ana
Luisito

subunits: 4

subunit-1: Caris (Andrés and Antonio, 50/50)
subunit-2: Parraquito (Ana and Luisito, 100/0)

Again, Caris are a couple and chose to split equally. Luis is 9y and travels with his mother Ana, so she pays for everything for the both of them.

Subunits are not just for couples, they can be for more people (there is currently not a limit, but it should be able to account for a whole family, maybe 8 people?). Mandatory: at least 1 person.

Now that subunits are introduced, from now on we'll talk about scopes.

- User: This is, a member on their own. When contributing money, paying fees, tipping, paying a ticket, it's for one and only one person, paying / adding the full amount.
- Subunit: This is, what we just saw. One member of the subunit can contribute, withdraw money, pay, ACCORDING to the weighted shares saved. This is important.
- Group: This is the wider scope. A member can contribute, withdraw money, pay in the name of the whole group. Everything is split EQUALLY, regardless members belonging to subunits with custom shares.

This is always true for contributions and withdrawals (including ATM fees), and it is the default behavior for expenses (later on, we will see this can be overridden PER expense to give more flexibility).

## Balances

This feature is only available if a group is selected.

Once a group has been created with members (either if they also created subunits or not, remember subunits are completely optional, it can be used only if users want to have a more fine-grained control), 
the first thing you might want to do is adding money.

It is important to notice that this app doesn't manage real money nor bank or credit card data, nor connects to third party apps related to user's bank accounts. It is purely to do maths to manage expenses.

In order to be able to talk about balances, I need to make a break and talk about other features before.

### Contributing

So, let's say a group of Spanish friends is planning a trip to Japan. This is,

group: Trip to Japan
default currency: EUR (this is the most natural currency for them for obvious reasons)
additional currencies: JPY, USD (JPY is used in Japan, plus maybe some hotels may charge in USD)
members: 5

Andrés
Antonio
Verónica
Carlos
Ana (16y)

subunits: 2

subunit-1: Borrachos (Andrés and Antonio, 50/50)
subunit-2: Familia (Verónica, Carlos and Ana, 30/50/20)

Since we already want to start tracking expenses, we need to add money to the group.

We decide to contribute 800 EUR each, so they give Andrés the money, and he adds it in the name of the group.

Andrés contributes 4000 EUR, scope: group. Immediately, each member has 800 EUR available to spend. (4000 / 5)

Then, Ana has some savings and want to share with her family, so she contributes 400 EUR in the name of the subunit 'Familia' (scope: subunit). This is split in the following way:

Verónica: 400 * 0.3 = 120 EUR
Carlos: 400 * 0.5 = 200 EUR
Ana: 400 * 0.2 = 80 EUR

Finally, Andrés feels he is going to spend so much in souvenirs, so he contributes 600 EUR for himself (scope: user).

Balances per user:

Andrés: 800 + 600 = 1400 EUR
Antonio: 800 EUR
Verónica: 800 + 120 = 920 EUR
Carlos: 800 + 200 = 1000 EUR
Ana: 800 + 80 = 880 EUR

Group total: 5000 EUR

Let's skip expenses for now, since it's more complex. (Because the normal scenario is that the group starts adding expenses like flight, insurance, etc, prior to starting the trip)

### Withdrawing

- At the moment, only withdrawing at an ATM is considered to get cash. 
- Eventually, some other methods may be added, like exchanging money in your home country via your local bank or any currency exchange service before leaving the country.

Let's imagine for a second that the group is already in Japan and want to get some cash.

Andrés gets 30,000 JPY @ 185.185185 = 162 EUR in the name of the group (scope: group)

So each member gets: 6,000 JPY

Besides, the ATM charges 200 JPY, and for some reason, applies a different exchange rate @ 160.551 = 1.25 EUR, which is also split evenly among group members (0.25 EUR each)

Per-user balance is


