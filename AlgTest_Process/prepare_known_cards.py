import os
import sys
from pathlib import Path
import shutil

ATR_NAMES_FILES = 'atr_cardname.csv'

def search_files(folder):
    for root, dirs, files in os.walk(folder):
        yield from [os.path.join(root, x) for x in files]


def get_files_to_process(walk_dir: Path, required_extension: str):
    files_to_process = []
    for file_name in search_files(walk_dir):
        if not os.path.isfile(file_name):
            continue
        file_ext = file_name[file_name.rfind('.'):]
        if file_ext.lower() != required_extension:
            continue
        files_to_process.append(file_name)

    return files_to_process


def prepare_card_profiles(input_base_dir: str, out_target_dir: str):
    ALGSUPPORT_DIR = input_base_dir + '/results/'
    PERF_FIXED_DIR = input_base_dir + '/performance/fixed/'
    PERF_VARIABLE_DIR = input_base_dir + '/performance/variable/'
    algsupport_files = get_files_to_process(Path(ALGSUPPORT_DIR), '.csv')
    perffixed_files = get_files_to_process(Path(PERF_FIXED_DIR), '.csv')
    perfvariable_files = get_files_to_process(Path(PERF_VARIABLE_DIR), '.csv')

    if not os.path.isdir(out_target_dir + '/results/'):
        os.mkdir(out_target_dir + '/results/')

    transformed_files = {}
    with open(input_base_dir + '/' + ATR_NAMES_FILES) as f:
        lines = f.readlines()

        for line in lines:
            line = line.strip()
            if len(line) == 0:
                continue
            atr, cardname = line.split(';')
            cardname_under = cardname.replace(' ', '_')
            transformed_files[cardname_under] = []
            target_dir = out_target_dir + '/results/' + cardname_under + '/'
            if not os.path.isdir(target_dir):
                os.mkdir(target_dir)
            for file_name in algsupport_files:
                if os.path.basename(file_name).startswith(cardname_under):
                    shutil.copy(file_name, target_dir)  # copy ALGSUPPORT_DIR/file_name to results/cardname/file_name
                    transformed_files[cardname_under].append(file_name)
            for file_name in perffixed_files:
                if os.path.basename(file_name).startswith(cardname_under):
                    shutil.copy(file_name, target_dir)  # copy ALGSUPPORT_DIR/file_name to results/cardname/file_name
                    transformed_files[cardname_under].append(file_name)
            for file_name in perfvariable_files:
                if os.path.basename(file_name).startswith(cardname_under):
                    shutil.copy(file_name, target_dir)  # copy ALGSUPPORT_DIR/file_name to results/cardname/file_name
                    transformed_files[cardname_under].append(file_name)

    for cardname in sorted(transformed_files.keys()):
        print(cardname)
        for file in transformed_files[cardname]:
            print('  ' + file)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print('Usage: prepare_known_cards.py base_folder_with_jcalgtest_results base_output_folder')
    else:
        prepare_card_profiles(sys.argv[1], sys.argv[2])
