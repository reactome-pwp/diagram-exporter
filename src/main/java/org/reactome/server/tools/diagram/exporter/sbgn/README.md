[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# SBGN exporter

Exports reactions and pathways to Systems Biology Graphical Notation ([SBGN](http://sbgn.github.io/sbgn/)).

## Exporting a Diagram object to SGBN

This exporter assumes that an instance of the Diagram interface is provided with the layout of either a pathway or a reaction.

### Basic usage
```java
//Assuming 'diagram' is an instance of org.reactome.server.tools.diagram.data.layout.Diagram

SbgnConverter converter = new SbgnConverter(diagram);
org.sbgn.bindings.Sbgn sbgn = converter.getSbgn();

```

