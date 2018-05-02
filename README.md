# recruit-app

A [re-frame](https://github.com/Day8/re-frame) application for the recruiter website.

## Documentation

- [Components](https://github.com/TheLadders/recruit-app/tree/master/src/cljs/recruit_app/components)
  - [Table](https://github.com/TheLadders/recruit-app/tree/master/src/cljs/recruit_app/components/table)
  - [Modal](https://github.com/TheLadders/recruit-app/tree/master/src/cljs/recruit_app/components/modal)

## Development Setup

* Follow the steps on [this](https://theladders.atlassian.net/wiki/spaces/REC/pages/24215606/Recruiter+Engineering+Getting+Started+Guide) page and provide ```recruiter``` as team name when running the script.
* Install [leiningen](https://leiningen.org/) and use ```lein upgrade 2.7.1``` to make sure you have version 2.7.1
* In Intellij IDE, checkout from version control and provide the necessary repo url or SSH key and any target local directory for your local copy of repo.
* Run ```lein deps``` in terminal within IntelliJ to install all dependencies.

## Development Mode
To start the application locally:
* Run ``` lein garden auto```. This automatically recompiles css files on change
* In a different terminal tab run:
   ```
    lein clean
    lein figwheel dev
     ```
     Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).