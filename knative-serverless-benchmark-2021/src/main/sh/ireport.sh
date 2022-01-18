#!/bin/bash -x

# code modified from: https://github.ibm.com/TYOS/cpe/blob/v2.pin/perf/record.sh
# - run under Red Hat
# - run as root on worker node

if [ $# -ne 2 ]; then
    echo "specify (PID) (perf.data path)"
    exit 1
fi

ps -eaf -q ${1}

# symfs=/tmp/symfs
# mkdir -p $symfs

perf_dir=`dirname $2`
if [ `ls $perf_dir | grep -c perf-.*\.map` -gt 0 ]; then
    cp $perf_dir/perf-*.map /tmp
fi

# cpid=$(crictl inspect --output go-template --template '{{.info.pid}}' ${1})
# mount --bind /proc/${1}/root ${sysfs}
sysctl kernel.kptr_restrict=0
# echo perf report default
# perf report --header --stdio -f -i $2 --symfs=/proc/${1}/root --kallsyms=/proc/kallsyms --vmlinux=/boot/vmlinuz-`uname -r` --no-children
# echo
echo perf report call graph
perf report -g --header --stdio -f -i $2 --symfs=/proc/${1}/root --kallsyms=/proc/kallsyms --vmlinux=/boot/vmlinuz-`uname -r` --no-children
sysctl kernel.kptr_restrict=1
# umount ${symfs}
rm -f /tmp/perf-*.map
# rmdir ${symfs}

