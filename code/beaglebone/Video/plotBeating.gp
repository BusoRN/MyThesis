#! /usr/bin/gnuplot

reset
# set title "Beating"
set xlabel "Time [s]"
set ylabel "Amplitude"
set grid
plot 'video_data.dat' title "Beating" with lines
set terminal jpeg
set output "Image.jpg"
replot