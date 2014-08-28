#!/bin/bash

pentaho_home=$1
setup_path=$2

cd $pentaho_home/design-tools/data-integration


echo Adding columns
$pentaho_home/design-tools/data-integration/pan.sh -file $setup_path/addTenantColumnsToSampleData.ktr 

echo Adding Tenant IDs 
$pentaho_home/design-tools/data-integration/pan.sh -file $setup_path/updateSteelWheelsWithTenantInfo.ktr 
echo $pentaho_home/design-tools/data-integration/pan.sh -file $setup_path/updateSteelWheelsWithTenantInfo.ktr 
