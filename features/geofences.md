---
layout: feature
title: Geo-fences
---

This long-waiting issue was added in scope of [issue #100](https://github.com/vitalidze/traccar-web/issues/100). List of geo-fences is available at the left side of screen on 'Geo-fences' tab.

![Geo-fences tab](http://i59.tinypic.com/kdmp34.png)

Once switched to that tab 'Add/Edit/Share/Delete' buttons at top are acting for Geo-fences. When current tab is 'Devices' then they are working for devices.

Adding new geo-fence
--------------------

To start adding new geo-fence click on 'Add' button when 'Geo-fences' tab is selected. New window pops up with geo-fence settings.

![Add geo-fence](http://i62.tinypic.com/34iqsdk.png)

Select it's type (line, polygon or circle) and start drawing. Depending on type of geo-fence drawing is done as follows:

* line - put line points on map with mouse pointer. Double-click on last point to finish drawing.

![Drawing line](http://i60.tinypic.com/bj62wp.png)

* polygon - put polygon points on map with mouse pointer. Double-click on last point to finish drawing.

![Drawing polygon](http://i58.tinypic.com/t5hpae.png)

* circle - put circle center on map with mouse pointer. Once center point is put drawing finishes.

![Drawing circle](http://i62.tinypic.com/24dpkyt.png)

Formatting (color, line width) is not applied while drawing from scratch. Once drawing is finished it goes to 'edit' mode, which has formatting applied. There are two ways to get back to 'drawing' mode from here: 'clear' drawing or change geo-fence type (which also clears drawing).

Once finished click 'Save' to save changes to the database.

Editing geo-fence
-----------------

To start editing select geo-fence in a list on 'Geo-fences' tab and click 'edit' button. Also 'edit' mode is activated once drawing is finished. All changes in formatting (like width/radius or color) are reflected immediately on geo-fence drawing.

![Editing polygon](http://i57.tinypic.com/35ic3rl.png)

Once finished click 'Save' to save changes to the database.

Deleting geo-fence
------------------

To delete a geo-fence select in a list on 'Geo-fences' tab, click 'Remove' on toolbar and confirm it's removal.