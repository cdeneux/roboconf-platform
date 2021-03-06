#!/bin/bash
# Startup script for a Roboconf agent on Docker
# Find the file containing the messaging configuration
msgConfigFile=$1
dump=./dump-parameters.txt

# Prepare configuration files
cd /usr/local/roboconf-agent
echo "# Agent configuration - DO NOT EDIT: Generated by Roboconf" > etc/net.roboconf.agent.configuration.cfg
echo "# Messaging configuration - DO NOT EDIT: Generated by Roboconf" > "$msgConfigFile"
echo "# Dump of the parameters passed by Roboconf" > "$dump"

# And update them
for p in "$@"
do
	echo "$p" >> "$dump"
	if [[ $p == msg.* ]] ;
		then 
			echo "${p:4}" >> "$msgConfigFile"
			echo "Written as" >> "$dump"
			echo "${p:4}" >> "$dump"
	else 
		if [[ $p == agent.* ]] ;
			then 
				echo "${p:6}" >> etc/net.roboconf.agent.configuration.cfg
				echo "Written as" >> "$dump"
				echo "${p:6}" >> "$dump"
		fi
	fi

	echo "" >> "$dump"
done

# Start Karaf
cd bin
./karaf
