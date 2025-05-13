# Kango, an auto-hosted Kanban

<p align="center">
<strong>This project is in development...</strong>
</p>

Kango is a desktop application where you can use a Kanban board in a very simple way. Oriented for personal use, you can manage your hobbies or projects as well as your daily life.  

It stands out for the possibility of configuring the database associated to your needs, although by default it will use PostgreSQL locally.

## :clapper: Preview

<p align="center">
<strong>Currently, there is no preview...</strong>
</p>

## :scroll:Table of Contents

1. [Requirement Analysis](#black_nib-requirement-analysis)
    1. [Entities](#black_joker-entities)
    1. [Type of Users](#busts_in_silhouette-type-of-users)
    1. [Functional Requirements](#wrench-functional-requirements)
    1. [Non Functional Requirements](#electric_plug-non-functional-requirements)
1. [Design](#straight_ruler-design)
    1. [Prototype](#airplane-navigation)
    1. [SQL Database](#dvd-sql-database)

### :black_nib: Requirement Analysis

#### :black_joker: Entities

Currently, there are 4 entities.

| Entities |
| :-: |
| [State](#state) |
| [Dashboard](#dashboard) |
| [Table](#table) |
| [Card](#card) |
| [Automation](#automation) |
| [Tag](#tag) |

##### State

The Status contains the setting of the application. All oriented to accesability.

The configuration will be:
- Color blind filter
- Font size
- Language

##### Dashboard

The board contains all tables, a name, a legend with the types of cards and a space to save or quickly access the files.

It also contains the automations, labels, and global and local card templates.

##### Table

The table contains a set of cards next to the table name.

##### Card

The card contains name, description, type (template or card), and other specific information.

The specific information:
- Attached links
- Deadtime
- Checklist
- Tags

##### Automation

The Automation contains a list of instruction to do when it is activated.

##### Tag

The card contains text and color to be put on a card.


#### :busts_in_silhouette: Type of Users

As the application is for personal use, and will not be public in the restrictive use of the word.

The code will be accessible to everyone, to be downloaded and configured to personal needs.

Therefore, it does not consist of any kind of user to use. It will be the state of the application the “user”.

#### :wrench: Functional Requirements

Here are the diferent actions that can be done in the application:

| User Histories | 
| :-- |
| UH-101 Show list of available Dashboard |
| UH-102 Access to Help Page |
| UH-103 Show adaptative help |
| UH-104 Access to Configuration Page |
| UH-105 Show configuration options |
| UH-106 Configure Accesibility |
| UH-107 Create Card Template (Globally) |
| UH-108 Create Automation (Globally) |
| UH-109 Create tags (Globally) |
| UH-110 Create Dashboard |
| UH-111 Access Dashboard |
| UH-112 Delete Dashboard |
| UH-113 Delete Dashboards |
| UH-114 Exit of the application |
| UH-201 Show list of available Table | 
| UH-202 Access to associated files of the table |
| UH-203 Change Dashboard's name |
| UH-204 Create Card Template (Locally) |
| UH-205 Create Automation (Locally) |
| UH-206 Create tags (Locally) |
| UH-207 Create Table |
| UH-208 See Table |
| UH-209 Change Table's name |
| UH-210 Move table position in the dashboard |
| UH-211 Delete Table |
| UH-212 Copy list of cards |
| UH-213 Move list of cards |
| UH-214 Move card to another table |
| UH-215 Move card position in the table |
| UH-216 Sort cards from a table |
| UH-217 Create Card |
| UH-301 See Card |
| UH-302 Update Card |
| UH-303 Delete Card |
| UH-304 Attach file to a Card |
| UH-305 Set deadline to a Card|
| UH-306 Set checklist to a Card |
| UH-307 Set Tags to a Card |
| UH-308 Set description to a Card |
| UH-309 Set color to a Card |

#### :electric_plug: Non Functional Requirements

| Non Functional Requirements |
| :-: |
| Oriented to Desktop |
| Responsive Design |
| Front-end must be implemented with React |
| Back-end must be implemented with Spring |
| It needs to connect with SQL Database |
| GUI must be minimalist and user-friendly |
| Usability & Accesibility |
| Languages must be English and Spanish  |

### :straight_ruler: Design

#### :bookmark_tabs: Prototype

A disposable prototype of the main screens is shown.

<p align="center">
  <img src="/docs/prototype/v1/Home_Page.png" alt="Prototype v1 - Home Page">
  <br>
  <small>Figure 1. Prototype v1 - Home Page</small>
</p>

<p align="center">
  <img src="/docs/prototype/v1/Dashboard_Page.png" alt="Prototype v1 - Dashboard Page">
  <br>
  <small>Figure 2. Prototype v1 - Dashboard Page</small>
</p>

#### :dvd: SQL Database

A relational database is used to contain all entities.

<p align="center">
  <img src="/docs/diagrams/ER_Model.svg" alt="ER-Diagram">
  <br>
  <small>Entity Relation Diagram 1. SQL Database</small>
</p>