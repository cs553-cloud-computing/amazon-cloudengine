#!/bin/bash

x=1; 

for i in *jpg; 
	do counter=$(printf %03d $x); 
		ln -s "$i" img"$counter".jpg; 
		x=$(($x+1)); 
done