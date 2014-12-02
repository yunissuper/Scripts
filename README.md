# <a name="scripts"></a>IJ BAR
[![Latest Release](https://img.shields.io/github/release/tferr/Scripts.svg?style=flat-square)](https://github.com/tferr/Scripts/releases)
[![Issues](https://img.shields.io/github/issues/tferr/Scripts.svg?style=flat-square)](https://github.com/tferr/Scripts/issues)
[![GPL License](http://img.shields.io/badge/license-GPL-blue.svg?style=flat-square)](http://opensource.org/licenses/GPL-3.0)

Welcome to the **IJ BAR**: A collection of <b>B</b>roadly <b>A</b>pplicable <b>R</b>outines for [ImageJ][ij1]/[Fiji][fiji], the de facto standard in scientific image processing in the life sciences.

The easiest way is to install these scripts is to use [Fiji][fiji] and subscribe to the [BAR update site](http://fiji.sc/BAR#Installation). Alternatively, you can navigate the collection using the sections below, and download individual routines as needed.
For more details refer to the [BAR documentation page][Fiji documentation].


## [Data Analysis][Analysis]
  Operations related to statistics, profiles, histograms and curve fitting.

  1. (ijm) [Distribution Plotter](./Data_Analysis/README.md#distribution-plotter)
  2. (bsh) [Find Peaks](./Data_Analysis/README.md#find-peaks)
  3. (bsh) [Fit Polynomial](./Data_Analysis/README.md#fit-polynomial)
  4. (bsh) [Multichannel Plot Profile](./Data_Analysis/README.md#multichannel-plot-profile)
  5. (bsh) [Plot Results](./Data_Analysis/README.md#plot-results)


## [Image Annotation][Annotation]
  Aiders for the annotation of scientific images.

  1. (ijm) [Combine Orthogonal Views](./Annotation/README.md#combine-orthogonal-views)
  2. (ijm) [ROI Color Coder](./Annotation/README.md#roi-color-coder)

## [Image Segmentation][Segmentation]
  Routines for partitioning images into analyzable parts.

  1. (java) [Shen-Castan Edge Detector](./Segmentation/README.md#shen-castan-edge-detector)
  2. (ijm) [Apply Threshold To ROI](./Segmentation/README.md#apply-threshold-to-roi)
  3. (ijm) [Clear Thresholded Pixels](./Segmentation/README.md#clear-thresholded-pixels)
  4. (ijm) [Remove Isolated Pixels](./Segmentation/README.md#remove-isolated-pixels)
  5. (ijm) [Segment Profile Tool](./Tools/README.md#segment-profile-tool)
  6. (ijm) [Threshold From Background](./Segmentation/README.md#threshold-from-background)
  7. (ijm) [Wipe Background](./Segmentation/README.md#wipe-background)


## [Neuronal Morphometry][Morphometry]
  Scripts related to the quantification of neuronal arbors and other tree-like structures.

  1. (bsh) [Strahler Analysis](./Morphometry/README.md#strahler-analysis)


## [Tools and Toolsets][Tools]
  Additions to the ImageJ toolbar.

  1. (ijm) [Calibration Menu](./Tools/README.md#calibration-menu)
  2. (ijm) [List Folder Menu](./Tools/README.md#list-folder-menu)
  3. (ijm) [Segment Profile Tool](./Tools/README.md#segment-profile-tool)
  4. (ijm) [Shortcuts Menu](./Tools/README.md#shortcuts-menu)
  5. (ijm) [ROI Manager Tools](./Tools/README.md#roi-manager-tools)
  6. (ijm) [Toolset Creator](./Tools/README.md#toolset-creator)


## [BAR plugins][Plugins]
  Maven project implementing the Java [plugins](./BAR/README.md#bar-plugins) bundled with BAR. The files organizing the [BAR menu](./BAR/README.md#bar-menu) are also described in this section.

## [Snippets][Snippets]
  A depository of generic and reusable scripts to be used as scripting templates, including:

  1. (py) [Median Filter](./Snippets/README.md#median-filter)
  2. (ijm) [Process Folder](./Snippets/README.md#process-folder)
  3. (bsh) [Search Snippets](./Snippets/README.md#search-snippets)


## Help?
 * Want to Contribute to BAR?
    * Thanks! Please, please do! See [here](https://guides.github.com/activities/contributing-to-open-source/) and [here](https://help.github.com/articles/fork-a-repo) for details on how to [fork](https://github.com/tferr/Scripts/fork) BAR or [here](https://help.github.com/articles/using-pull-requests) on how to initiate a [pull request](https://github.com/tferr/Scripts/pulls)
    * Documentation updates are also welcome, so go ahead and improve the [BAR documentation page][Fiji documentation]
 * Having problems? Found a bug? Need to ask a question?
    * See the BAR [FAQs](http://fiji.sc/BAR#FAQ), Fiji [FAQs](http://fiji.sc/Frequently_Asked_Questions) and [Bug reporting best practices](http://fiji.sc/Bug_reporting_best_practices). Then, you can either:
      * [Open an issue](https://github.com/tferr/Scripts/issues) on this repository
      * Report it on the [ImageJ mailing list](http://imagej.nih.gov/ij/list.html)


## Citations
BAR scripts have contributed to the following publications:

  1. Ferreira et al. Neuronal morphometry directly from bitmap images.Nature Methods (2014), 11(10):982–984. [PMID 25264773](http://www.ncbi.nlm.nih.gov/pubmed/25264773)
  1. Pope and Voigt. Peripheral glia have a pivotal role in the initial response to axon degeneration of peripheral sensory neurons in zebrafish. PLoS ONE (2014), 9(7):e103283. [PMID 25058656](http://www.ncbi.nlm.nih.gov/pubmed/25058656)
  1. Medda et al. Investigation of early cell-surface interactions of human mesenchymal stem cells on nanopatterned β-type titanium-niobium alloy surfaces. Interface Focus (2014), 4(1):20130046. [PMID 24501674](http://www.ncbi.nlm.nih.gov/pubmed/24501674)
  1. Ferreira et al. Dendrite architecture is organized by transcriptional control of F-actin nucleation. Development (2014), 141(3):650–60. [PMID 24449841](http://www.ncbi.nlm.nih.gov/pubmed/24449841)
  1. Dobens and Dobens. FijiWings: an open source toolkit for semiautomated morphometric analysis of insect wings. G3 (Bethesda) (2013), 3(8):1443-9. [PMID 23797110](http://www.ncbi.nlm.nih.gov/pubmed/23797110)
  1. van der Meer et al. Three-dimensional co-cultures of human endothelial cells and embryonic stem cell-derived pericytes inside a microfluidic device. Lab Chip (2013), 13(18):3562-8. [PMID 23702711](http://www.ncbi.nlm.nih.gov/pubmed/23702711)
  1. Soulet et al. Automated filtering of intrinsic movement artifacts during two-photon intravital microscopy. PLoS ONE (2013), 8(1):e53942. [PMID 23326545](http://www.ncbi.nlm.nih.gov/pubmed/23326545)
  1. Paolicelli et al. Synaptic pruning by microglia is necessary for normal brain development. Science (2011), 9;333(6048):1456-8. [PMID 21778362](http://www.ncbi.nlm.nih.gov/pubmed/21778362)
  1. Carnevalli et al. S6K1 plays a critical role in early adipocyte differentiation. Developmental Cell (2010), 18(5):763-74. [PMID 20493810](http://www.ncbi.nlm.nih.gov/pubmed/20493810)


## References
  - Shen and Castan. An optimal linear operator for step edge detection. CVGIP: Graphical Models and Image Processing (1992) vol. 54 (2) pp. 112-133 [DOI: 10.1016/1049-9652(92)90060-B](http://dx.doi.org/10.1016/1049-9652(92)90060-B)


License
-------
These scripts are free software: you can redistribute them and/or modify them under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl.txt) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.


Contributors
------------
BAR was created and is maintained by [Tiago Ferreira](mailto:tiagoalvespedrosa_at_gmail_dot_com) with contributions from Maxime Pinchon, [Johannes Schindelin](https://github.com/dscho), [Wayne Rasband][ij1], [Jerome Mutterer](https://github.com/mutterer) and [Kota Miura](https://github.com/cmci). This project would not have been possible without the support of the outstanding [ImageJ community](http://imagej.net/Mailing_Lists).


[ij1]: http://imagej.nih.gov/ij/
[fiji]: http://fiji.sc/





| [Analysis] | [Annotation] | [Segmentation] | [Morphometry] | [Tools] | [Plugins] | [Snippets] | [Fiji][Fiji documentation] |
|:----------:|:------------:|:--------------:|:-------------:|:-------:|:---------:|:----------:|:--------------------------:|


[Analysis]: https://github.com/tferr/Scripts/tree/master/Data_Analysis#analysis
[Annotation]: https://github.com/tferr/Scripts/tree/master/Annotation#annotation
[Segmentation]: https://github.com/tferr/Scripts/tree/master/Segmentation#segmentation
[Morphometry]: https://github.com/tferr/Scripts/tree/master/Morphometry#morphometry
[Tools]: https://github.com/tferr/Scripts/tree/master/Tools#tools-and-toolsets
[Plugins]: https://github.com/tferr/Scripts/tree/master/BAR#bar-plugins
[Snippets]: https://github.com/tferr/Scripts/tree/master/Snippets#snippets
[Fiji documentation]: http://fiji.sc/BAR

