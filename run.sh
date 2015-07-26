#!/usr/bin/env bash
set -ex
if [ -z "$@" ]; then
	gradle run
else
	printf -v var "'%s', " "$@"
	var=${var%??}
	gradle run -PappArgs="[$var]"
fi

