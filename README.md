# [DEMO](https://d-bl.github.io/GroundForge/)
A web based toolbox to design bobbin lace grounds with matching diagrams.

[TesseLace.com]: http://TesseLace.com

## How it's Made / Under the Hood

### Proof of concept with D3.js

Development started with `js/show-graph.js` and `js/sample.js`.
To get a proof of concept a force graph [example] with [D3.js] was changed into tiny thread an pair diagrams diagrams with the following steps:

- Replaced the server side JSon with the hard-coded `js/sample.js` assembled from a manual sketch `js/sample.png`.
- Applied arrow heads and flattened them to line ends to emulate [color coded pair diagrams] or to emulate the over/under effect in thread diagrams.
- Made nodes transparent except for bobbins.
- Assigned the thread number as a class to each section of a thread to assign colors.
- Turned the links from lines to paths with a third node to add mid-markers for twist marks.
- Initial coordinates replace the default random values, thus the animation stabalizes much quicker and it prevents rotated and flipped diagrams.

### Using data from TesseLace

To provide patterns [scala code] transforms a selection of matrices generated by [TesseLace.com] into alternatives for the `js/sample.js`.
The matrices contain geometric information used to initialise the diagrams, speeding up the animation as explained above.
The diagrams lack the original geometric information after completion of the animation, so topological duplicates were removed from the selection of matrices.
Downloadable pattern sheets provide geometric variations that can be customised into intermediate and other variations.


[example]: http://bl.ocks.org/mbostock/4062045
[D3.js]: http://d3js.org/
[color coded pair diagrams]: https://en.wikipedia.org/w/index.php?title=Mesh_grounded_bobbin_lace&oldid=639789191#Worker_pair_versus_two_pair_per_pin
[scala code]: https://github.com/d-bl/GroundForge/tree/master/

### Color-picker by menucool

Painting threads required a color-picker. Among the paid ui elements the [color-picker](http://www.menucool.com/color-picker) was free.

## How to Contribute

You may just improve the grammar on the demo-page or on this readme, improve the layout or fix a more technical issue.

Don't know about version control in general or GitHub in particular? No problem:
* just create a github [account](https://github.com)
* hit the fork button at the top of this page
* go to `https://github.com/YOURID/GroundForge/tree/gh-pages/`, of course replace YOURID
* choose the file you want to change and hit the pencil to start editing
* save your changes and test with your own demo-page: `http://YOURID.github.io/GroundForge/`, again: replace YOURID
* create a pull request at `https://github.com/YOURID/GroundForge/tree/gh-pages/` or drop a note
