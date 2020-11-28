import os
import sys
import shutil
import subprocess


def search_files(folder):
    for root, dirs, files in os.walk(folder):
        yield from [os.path.join(root, x) for x in files]


def process_file(file_name, enable_api_map):
    with open(file_name, 'r') as f:
        lines = f.readlines()

    output_lines = []
    for line in lines:
        output_line = line
        for api in enable_api_map.keys():
            if line.find(api) != -1:
                # we found line with api to enable or disable
                if enable_api_map[api]:
                    # make sure there is no '//' at the begin of the line
                    if line.startswith('//'):
                        output_line = line[2:]
                else:
                    # add '//' at the beginning of line (if not already)
                    if not line.startswith('//'):
                        output_line = '//' + line

        output_lines.append(output_line)

    with open(file_name, 'w') as f:
        f.writelines(output_lines)


def process_api_version(source_dir, target_dir, build_name, enable_api_map):
    if os.path.isdir(target_dir):
        shutil.rmtree(target_dir)
    shutil.copytree(source_dir, target_dir)
    for file_name in search_files(target_dir):
        process_file(file_name, enable_api_map)

    returned_text = subprocess.check_output('ant -f jcbuild.xml ' + build_name, shell=True, universal_newlines=True)
    print(returned_text)

    #os.system('ant -f jcbuild.xml ' + build_name)


def main(argv):
    # JC 2.2.2 API
    process_api_version('src/', 'src222/', 'build222', {'//jc304': False, '//jc305': False})
    # JC 3.0.4 API
    process_api_version('src/', 'src304/', 'build304', {'//jc304': True, '//jc305': False})
    # JC 3.0.5 API
    process_api_version('src/', 'src305/', 'build305', {'//jc304': True, '//jc305': True})


    print('All files processed and converted')


if __name__ == '__main__':
    main(sys.argv)