#!/bin/sh -e
#montage -geometry x116 p06-33-felhok_tanca.jpg p06-34-felhok_tanca.jpg p06-37-felhok_meredeken.jpg -mode concatenate -bordercolor white -border 2 -frame 0 tmp.tif
#convert -crop 400x116+2+2 tmp.tif -quality 75 p06-33+34+37.jpg

montage -geometry x150 p05-27-G+D_felulrol.jpg p05-31-G+D+Gy_felulrol.jpg -mode concatenate -bordercolor white -border 2 -frame 0 tmp.tif
convert -crop 328x150+2+2 tmp.tif -quality 75 p05-27+31-pici.jpg

rm tmp.tif
