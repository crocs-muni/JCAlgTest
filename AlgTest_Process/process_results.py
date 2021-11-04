import json
import click
from pathlib import Path
import os

MEASUREMENT_CATEGORIES = ["MESSAGE DIGEST", "RANDOM GENERATOR", "CIPHER", "SIGNATURE", "CHECKSUM",
             "AESKey", "DESKey", "KoreanSEEDKey", "DSAPrivateKey", "DSAPublicKey",
             "ECF2MPublicKey", "ECF2MPrivateKey", "ECFPPrivateKey", "ECFPPublicKey", "HMACKey",
             "RSAPrivateKey", "RSAPublicKey", "RSAPrivateCRTKey", "KEY PAIR", "UTIL",
             "SWALGS", "KEYAGREEMENT"]

CARD_EXCEPTION_TO_STRING = {
    'f101': 'CryptoException_ILLEGAL_VALUE',
    'f102': 'CryptoException_UNINITIALIZED_KEY',
    'f103': 'CryptoException_NO_SUCH_ALGORITHM',
    'f104': 'CryptoException_INVALID_INIT',
    'f105': 'CryptoException_ILLEGAL_USE',
    'f201': 'SystemException_ILLEGAL_VALUE',
    'f202': 'SystemException_NO_TRANSIENT_SPACE',
    'f203': 'SystemException_ILLEGAL_TRANSIENT',
    'f204': 'SystemException_ILLEGAL_AID',
    'f205': 'SystemException_NO_RESOURCE',
    'f206': 'SystemException_ILLEGAL_USE',
    'f301': 'PINException_ILLEGAL_VALUE',
    'f302': 'PINException_ILLEGAL_STATE',
    'f401': 'TransactionException_IN_PROGRESS',
    'f402': 'TransactionException_NOT_IN_PROGRESS',
    'f403': 'TransactionException_BUFFER_FULL',
    'f404': 'TransactionException_INTERNAL_FAILURE',
    'f405': 'TransactionException_ILLEGAL_USE',
    'f500': 'CardRuntimeException',
    'ff01': 'Exception_GENERIC',
    'ff02': 'ArrayIndexOutOfBoundsException',
    'ff03': 'ArithmeticException',
    'ff04': 'ArrayStoreException',
    'ff05': 'NullPointerException',
    'ff06': 'NegativeArraySizeException',
    'ff': 'Exception_GENERIC',
    '6a81': 'FUNC_NOT_SUPPORTED',
    '6f00': 'Exception_GENERIC'
}

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


def extract_section(lines: list, start_string: str, perf_measurement: bool):
    all_sections = []
    i = 0
    while i < len(lines):
        if lines[i].startswith(start_string):  # detect start of the section to extract
            just_entered = True
            section_items = {}
            while i < len(lines) and len(lines[i]) > 0:  # process section till its end (newline)
                if not just_entered and lines[i].startswith(start_string):   # check if we hit another section
                    # we hit start fo another section - finish and let next section to be processed
                    i = i - 1
                    break

                pos = lines[i].find(';')
                if pos == -1:
                    pos = len(lines[i])
                if pos > 0:
                    key = lines[i][0: pos].strip()
                    if lines[i].find('method name:;') != -1:
                        # do not strip ending ; for line with method for variable data measurements, strip only starting
                        # method_name;data_length;
                        value = lines[i][pos:].lstrip(';').strip()
                    else:
                        # strip ending ;
                        value = lines[i][pos:].strip().strip(';').strip()

                    if perf_measurement and len(value) == 0:  # error status like NO_SUCH_ALGORITHM
                        section_items['status'] = key
                    else:
                        if lines[i].find('Exception') != -1:  # various exceptions
                            section_items['status'] = key
                        else:
                            section_items[key] = value
                i = i + 1
                just_entered = False

            all_sections.append(section_items)
        i = i + 1

    return all_sections


def update_if_not_empty(struct: dict, values: list):
    if len(values) > 0:
        struct.update(values[0])


def convert_to_json(walk_dir: str):
    files = get_files_to_process(walk_dir, '.csv')

    for filename in files:
        print(filename)

        with open(filename) as f:
            values = {}
            lines_temp = f.readlines()
            lines = [line.rstrip('\n') for line in lines_temp]

            values['Info'] = {}
            update_if_not_empty(values['Info'], extract_section(lines, 'INFO:', False))
            update_if_not_empty(values['Info'], extract_section(lines[len(lines) - 10:], 'Total test time:;', False))
            update_if_not_empty(values['Info'], extract_section(lines[len(lines) - 10:], 'Total human interventions (retries with physical resets etc.):;', False))
            update_if_not_empty(values['Info'], extract_section(lines[len(lines) - 10:], 'Total reconnects to card:;', False))

            values['JCSystem'] = {}
            update_if_not_empty(values['JCSystem'], extract_section(lines, 'JCSystem.getVersion()', False))
            update_if_not_empty(values['JCSystem'], extract_section(lines, 'JavaCard support version', False))

            values['CPLC'] = {}
            update_if_not_empty(values['CPLC'], extract_section(lines, 'CPLC;', False))

            values['Measurements'] = {}
            for category in MEASUREMENT_CATEGORIES:
                values['Measurements'][category] = {}
                # find start and end of the specific category
                i = 0
                start_index = -1
                end_index = -1
                while i < len(lines):
                    if lines[i].startswith(category):
                        start_index = i
                        # find end of section
                        while i < len(lines):
                            if lines[i].startswith(category + ' - END'):
                                end_index = i
                                break
                            i = i + 1

                        if start_index > -1 and end_index > -1:
                            section_data = extract_section(lines[start_index: end_index], 'method name:;', True)
                            for item in section_data:
                                if len(item.keys()) == 7:  # add explicit OK for correctly measured sections
                                    item['status'] = 'OK'

                                if item['method name:'] in values['Measurements'][category].keys():
                                    print('Already exists ' + item['method name:'] + filename)
                                values['Measurements'][category][item['method name:']] = item

                    i = i + 1

            with open(filename + ".json", "w") as write_file:
                json.dump(values, write_file, indent=2, sort_keys=False)


            # # sanity check
            # for item in values['Measurements']:
            #     for item2 in item:
            #         lines.remove()


def prepare_missing_measurements(walk_dir: str):
    files = get_files_to_process(walk_dir, '.json')
    for filename in files:
        print(filename)
        with open(filename) as json_file:
            measurements = json.load(json_file)

            # find all properly measured operations
            correctly_measured = []
            measured_with_errors = []
            msr = measurements['Measurements']
            for category in msr:
                for ops in msr[category].keys():
                    if 'status' not in msr[category][ops]:
                        print(msr[category][ops])
                    status = msr[category][ops]['status']
                    if status == 'OK' or status == 'NO_SUCH_ALGORITHM' or status.find('FUNC_NOT_SUPPORTED') != -1:
                        correctly_measured.append(ops + '\n')
                        measured_with_errors.append(ops + '\n')
                    else:
                        measured_with_errors.append(ops + ' ' + status + '\n')

            print(filename)
            out_file_name = walk_dir + measurements['Info']['Card name'].replace(' ', '_') + '____PERFORMANCE_SYMMETRIC_ASYMMETRIC_DATAFIXED__already_measured.list'
            correctly_measured.sort()
            measured_with_errors.sort()
            with open(out_file_name, 'w') as f:
                f.writelines(correctly_measured)
            with open(out_file_name + '.with_errors', 'w') as f:
                f.writelines(measured_with_errors)


def fix_missing_underscores(walk_dir: str, correct_ops_names: list):
    files = get_files_to_process(walk_dir, '.csv')

    # create list of operations without underscores
    correct_ops_names_no_underscore = []
    for item in correct_ops_names:
        correct_ops_names_no_underscore.append(item.replace('_', ' ').strip())

    for filename in files:
        print(filename)

        change_found = False
        with open(filename) as f:
            values = {}
            lines = f.readlines()
            lines_corrected = []
            for line in lines:
                i = 0
                line_corrected = line
                if line.find('method name:;') != -1:
                    line_updated = False
                    # two specifically known issues
                    if line.find('TYPE_EC_FP PRIVATE LENGTH_EC_FP') != -1:
                        line_corrected = line.replace('TYPE_EC_FP PRIVATE LENGTH_EC_FP', 'TYPE_EC_FP_PRIVATE LENGTH_EC_FP')
                        line_updated = change_found = True
                    if line.find('TYPE_DSA_PRIVATE LENGTH DSA_1024') != -1:
                        line_corrected = line.replace('TYPE_DSA_PRIVATE LENGTH DSA_1024', 'TYPE_DSA_PRIVATE LENGTH_DSA_1024')
                        line_updated = change_found = True

                    # all other potential issues against known-good template
                    if not line_updated:
                        while i < len(correct_ops_names_no_underscore):
                            pos = line.find(correct_ops_names_no_underscore[i])
                            if pos != -1:
                                print('  ' + line)
                                pre_part = line[0:pos]
                                post_part = line[pos + len(correct_ops_names_no_underscore[i]):]
                                line_corrected = pre_part + correct_ops_names[i].strip() + post_part
                                change_found = True
                                break
                            i = i + 1
                lines_corrected.append(line_corrected)

            if change_found:
                with open(filename, 'w') as f_write:
                    f_write.writelines(lines_corrected)


def fix_missing_variable_data_lengths(walk_dir: str):
    files = get_files_to_process(walk_dir, '.csv')

    for filename in files:
        print(filename)

        change_found = False
        with open(filename) as f:
            values = {}
            lines = f.readlines()
            lines_corrected = []
            index = 0
            while index < len(lines):
                line = lines[index]
                i = 0
                line_corrected = line
                if line.find('method name:;') != -1:
                    if line.rstrip().endswith('()'):  # add data length only to the measurements where it is missing
                        # Option 1: look ahead and extract length from measurement config
                        config_line = lines[index + 1]
                        if config_line.find('measurement config:') == -1:
                            print('ERROR: missing measurement config on line ' + str(index + 1))
                        else:
                            # measurement config:;appletPrepareINS;34;appletMeasureINS;41;config;00 15 00 02 ff ff ff ff ff ff 00 06 00 10 ff ff ff ff 00 05 00 01
                            pos = config_line.find(';config;') + 44  # jump to payload with data length
                            data_len_chars = config_line[pos: pos + 5].replace(' ', '')
                            data_len = int(data_len_chars, 16)
                            data_len_verif = 0
                            # do sanity check, data_len shall be 16, 32, 64, 128, 256 or 512 or not higher than 512
                            if data_len not in [16, 32, 64, 128, 256, 512]:
                            #if data_len > 512:
                                print('WARNING: unexpected variable data length ' + str(data_len))
                            else:
                                # verify against 'data length' item in 'operation info:' (if available)
                                info_line = lines[index + 6]
                                if info_line.find('operation info:') != -1:
                                    # operation info:;data length;16;total iterations;250;total invocations;250;
                                    # operation info:;data length;-1;total iterations;250;total invocations;250;
                                    items = info_line.split(';')
                                    data_len_verif = int(items[2])

                                if data_len_verif == 0 or data_len == data_len_verif:
                                    line_corrected = '{};{};\n'.format(line.strip(), data_len)
                                    change_found = True
                                else:
                                    print('ERROR: mismatch in extracted data lengths ' + str(data_len) + ' vs. ' + str(data_len_verif))

                lines_corrected.append(line_corrected)
                index = index + 1

            if change_found:
                with open(filename, 'w') as f_write:
                    f_write.writelines(lines_corrected)


def fix_error_codes(walk_dir: str):
    files = get_files_to_process(walk_dir, '.csv')

    for filename in files:
        print(filename)

        change_found = False
        with open(filename) as f:
            lines = f.readlines()
            lines_corrected = []
            index = 0
            while index < len(lines):
                line = lines[index]
                i = 0
                line_corrected = line
                if line.find('UNKONWN_ERROR-card_has_return_value') != -1:
                    pos = line.find('UNKONWN_ERROR-card_has_return_value') + len('UNKONWN_ERROR-card_has_return_value') + 1
                    error_code = line[pos:]
                    error_code = error_code.strip()

                    line_corrected = CARD_EXCEPTION_TO_STRING.get(error_code, error_code) + ' (' + error_code + ')\n'
                    change_found = True

                lines_corrected.append(line_corrected)
                index = index + 1

            if change_found:
                with open(filename, 'w') as f_write:
                    f_write.writelines(lines_corrected)


def compute_stats(walk_dir: str):
    stats = {}

    files = get_files_to_process(walk_dir, '.json')
    for filename in files:
        with open(filename) as json_file:
            measurements = json.load(json_file)

            # find all properly measured operations
            msr = measurements['Measurements'];
            for category in msr:
                for ops in msr[category].keys():
                    if ops not in stats:
                        stats[ops] = 0
                    stats[ops] = stats[ops] + 1

    stats_sorted = dict(sorted(stats.items(), key=lambda item: item[1]))
    with open("stats.json", "w") as write_file:
        json.dump(stats_sorted, write_file, indent=2, sort_keys=False)


def create_sorted_already_measured_list(directory: str):
    with open(directory + 'template____PERFORMANCE_SYMMETRIC_ASYMMETRIC_DATAFIXED__already_measured.list') as f:
        all_to_measure_ops = f.readlines()
        all_to_measure_ops.sort()
        out_file_name = directory + 'sorted____PERFORMANCE_SYMMETRIC_ASYMMETRIC_DATAFIXED__already_measured.list'
        with open(out_file_name, 'w') as f_write:
            f_write.writelines(all_to_measure_ops)

    with open(directory + 'template____PERFORMANCE_SYMMETRIC_ASYMMETRIC_DATADEPEND__already_measured.list') as f:
        all_to_measure_ops = f.readlines()
        all_to_measure_ops.sort()
        out_file_name = directory + 'sorted____PERFORMANCE_SYMMETRIC_ASYMMETRIC_DATADEPEND__already_measured.list'
        with open(out_file_name, 'w') as f_write:
            f_write.writelines(all_to_measure_ops)

    return all_to_measure_ops


@click.command()
@click.argument("directory", required=True, type=str)
@click.option("--output-dir", "output_dir", type=str,  help="Base path for output.")
def main(directory: str, output_dir: str):
    all_to_measure_ops = create_sorted_already_measured_list(directory)

    fix_error_codes(directory)  # error codes not translated into human readable string _

    fix_missing_underscores(directory, all_to_measure_ops)  # some file had incorrect naming for measured values without _
    fix_missing_variable_data_lengths(directory)

    convert_to_json(directory)  # from csv to json

    prepare_missing_measurements(directory)  # prepare *__already_measured.list files to collect missing measurements

    compute_stats(directory)


if __name__ == "__main__":
    main()
