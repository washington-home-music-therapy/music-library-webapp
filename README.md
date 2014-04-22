music-library-webapp
====================

A Spring Boot webapp used to coordinate nonprofit therapeutic music sharing.

The application is a Java Spring Boot MVC webapp. Currently it will parse an iTunes library into a data model and store the file to local disk. It has no real front end yet.

The current architectural direction and priority is:

1. Jackson JSON view for the parsed data
2. Hibernate / MySql for persistence
3. Handlebars templates for the user views
4. Views and business logic as outlined in Larry's crawl/walk/run document
5. Connection with discogs.com for better media metadata
6. Explore neo4j graph db for music preference scoring