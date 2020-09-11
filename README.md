# Comptes

## What is it ?
A Java Desktop app intended to manage personal bank accounts.

This is a full-customized application for the own purposes of the original author, but it could be used by anyone who is comfortable with his point of view.

In fact, it was the very first real application in Java written by the original author. Now you know why he wanted to learn this language at the beginning. ;-)

Last but not least : it was written in french only, even the commit comments that are in bad frenglish. Yes I know, shame on me for this newbie behaviour.

## What it does

* Following bank accounts
* Categorizing revenues and expenses
* Automatic generation of monthly repetitive operations
* Warning about the lowest previewed level of each bank account for the next month
* Displaying diagrams
* Separating main accounts, spare ones, and children ones. This enables to follow spare movements in both directions, and also spare capacity.

## How to contribute

You're welcome to copy or contribute.

The next development goals are :

* Rewriting the GUI with JavaFX instead of Swing.
Most of all, the actual code of the main window sucks...
The layout should be redesigned at the same time, with :
  * a full list of all accounts on the left panel (bank, revenues and expenses) to allow easy browsing from wherever ;
  * a form to add and edit easily an operation above the table, instead of doing it in the JTable cells ;
  * a separated view for accounts evolutions and means, and a third for full text search.
    At this time the main window is splitted into account categories and these features are lying inside each category, which is painful ;
  * using a framework for diagrams. Actual ones are completely home made, which has a special taste but looks still a bit ugly.

* Write a new DAO using Hibernate.
  At this point, the model is saved as a zip archive containing CSV and XML files.
  This is accurate but not standard at all, and a bit "re-inventing the wheel".
  Hibernate is the adequate solution and could be used alternatively with the actual DAO implementation.
  But of course, it needs to modify the core classes as a side effect.

* Split the bank date ("pointage") in two : one for the account of origin, the other for the target account.
  When they remain in two different banks, there may be a few days of difference.

## GPLv3 License

    (c) 2009-2020 Olivier Haas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>
