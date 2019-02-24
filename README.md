# XML-to-TSV-for-BNF-files
This project is an implementation of a converter of XML files describing manuscripts from the [BNF](https://www.bnf.fr), to CSV files, which is usable by non-technical people.

The XML files it takes as inputs have a clearly defined structure, but as they are written by hand by non-technical people, this structure is often not respected or misinterpreted.
Moreover, some historians use a personal database that they crafted over the years prior to the definition of this XML standard, and that they want to keep expanding with new data.
As can be expected, those database systems cannot import information from XML files (especially if they don't conform to the standardized structure).

The Tab-Separated-Value files produced should thus contain all the relevant information from the corresponding XML files,
and should be easy to import in a database system.
Plus the converter should be usable by non-technical people.

This project thus contains:
- A custom XML parser, that accept syntactically incorrect XML files and tries to cope with them as much as possible.
- A converter from an internal representation of the (sometimes guessed) structure of the input XML files to CSV files containing only the relevant information.

This project was made in 2017.

*Languages used:*
- *Java*

## Collaborators
I worked alone on this project, with input from the Professor in History who was the client.

## What I learned
- To make an XML parser
- To try and cope with user data that contains invalid/unexpected information and formats
- To analyze, understand and find a solution to a client's problem
- To fulfill a client's demand
- To use Java Swing library to make a simple and ergonomic GUI
- To make a multi-language GUI (here in 2 languages)

## Files worth checking out
- The main part of the XML parser: [backend/parser/Parser.java](https://github.com/SimGus/XML-to-CSV-for-BNF-files/blob/master/backend/parser/Parser.java)
- The class that finds, opens and runs the translation of each of the XML files selected: [backend/transcripter/Translator.java](https://github.com/SimGus/XML-to-CSV-for-BNF-files/blob/master/backend/transcripter/Translator.java)
- The class that selects the important information in the XML file: [backend/transcripter/Interpreter.java](https://github.com/SimGus/XML-to-CSV-for-BNF-files/blob/master/backend/transcripter/Interpreter.java)

## Compilation and execution
Compile the project:
```sh
make all
```

Run the project:
```sh
make run
```
