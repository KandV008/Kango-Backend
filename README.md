# Kango, a personal kanban

<p align="center">
<strong>This project is only an idea...</strong>
</p>

Kango is a desktop application where you can use a Kanban board in a very simple way. Oriented for personal use, you can manage your hobbies or projects as well as your daily life.  

It also allows you to group files to have them in easy access and to be able to count the time spent on what you want.

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

Currently, there are 2 entities.

| Entities |
| :-: |
| [User](#user) |
| [Dashboard](#dashboard) |
| [Table](#table) |
| [Card](#card) |

##### User

See this section to know more about the [Entity User](#busts_in_silhouette-type-of-users).

##### Dashboard

The board contains all tables, a name, a description, a timer, a legend with the types of cards and a space to save or quickly access the files.

##### Table

The table contains a set of cards next to the table name.

##### Card

The card contains name, description, type, and other specific functionalities.

#### :busts_in_silhouette: Type of Users

In the application there are 2 different type of user:

| Type of User | Attributes |
| :-: | :-- |
| Unregistered User | It can not use the application |
| Registered User | It can use the application |

#### :wrench: Functional Requirements

Here are the diferent actions that can do the different type of users:

| User Histories | Unregistered User | Registered User |
| :-- | :-: | :-: |
| UH-01 Sign Up | :heavy_check_mark:  | |
| UH-02 Log In | | :heavy_check_mark: |
| UH-03 Log Out | | :heavy_check_mark: |
| UH-04 Delete Account | | :heavy_check_mark: |
| UH-05 Create Dashboard | | :heavy_check_mark: |
| UH-06 Access Dashboard | | :heavy_check_mark: |
| UH-07 Update Dashboard (To expand) | | :heavy_check_mark: |
| UH-08 Delete Dashboard | | :heavy_check_mark: |
| UH-09 See List of Dashboards | | :heavy_check_mark: |
| UH-10 Create Table | | :heavy_check_mark: |
| UH-11 See Table | | :heavy_check_mark: |
| UH-12 Update Table (To expand) | | :heavy_check_mark: |
| UH-13 Delete Table | | :heavy_check_mark: |
| UH-14 Create Card | | :heavy_check_mark:  |
| UH-15 See Card | | :heavy_check_mark: |
| UH-16 Update Card (To expand) | | :heavy_check_mark: |
| UH-17 Delete Card | | :heavy_check_mark: |
| UH-18 Access Configuration Page | | :heavy_check_mark: |
| UH-19 Access Help Pop Ups | | :heavy_check_mark: |

#### :electric_plug: Non Functional Requirements

| Non Functional Requirements |
| :-: |
| Oriented to Desktop |
| Responsive Design |
| Front-end must be implemented with React |
| Back-end must be implemented with Django |
| It needs to connect with SQL Database |
| GUI must be minimalist and user-friendly |
| Protection & Security for Registered User Data |
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