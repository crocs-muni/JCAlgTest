#%%
import random
import graphviz, re, time, os, sys, glob, json, ntpath
import matplotlib.cbook as cbook
from pathlib import Path
from graphviz import Digraph


def search_files(folder):    
    for root, dirs, files in os.walk(folder):
        yield from [os.path.join(root, x) for x in files]

def path_leaf(path):
    head, tail = ntpath.split(path)
    return tail or ntpath.basename(head)

# CPLC info collected from:
# own analysis
# https://www.javatips.net/api/bankomatinfos-master/src/at/zweng/bankomatinfos/iso7816emv/CPLC.java
# NIST FIPS140, CC EAKL certificates
'''
Returns human-readable name of fabricator based on provided ICFabricator id
@param icfab: string realization of 2 bytes ICFabricator (in hexadecimal, e.g., '4090')
@returns human-readable fabricator string
'''
def get_fab_name(icfab):
    if icfab.find('0003') != -1: return 'Renesas' # https://www.cryptsoft.com/fips140/vendors/140sp485.pdf
    if icfab.find('0005') != -1: return 'Infineon' # https://csrc.nist.gov/csrc/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp2327.pdf
    if icfab.find('008c') != -1: return 'Tongxin' # https://csrc.nist.gov/csrc/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp2327.pdf
    if icfab.find('2050') != -1: return 'Philips' # https://www.commoncriteriaportal.org/files/epfiles/ANSSI-CC-2007-02-M02fr.pdf, pp. 2
    if icfab.find('3060') != -1: return 'Renesas'
    if icfab.find('4070') != -1: return 'NXP' # https://csrc.nist.rip/groups/STM/cmvp/documents/140-1/140sp/140sp963.pdf, pp. 4
    if icfab.find('4090') != -1: return 'Infineon'
    if icfab.find('4180') != -1: return 'Atmel'
    if icfab.find('4250') != -1: return 'Samsung'
    if icfab.find('4790') != -1: return 'NXP'
    if icfab.find('4830') != -1: return 'Infineon'
    if icfab.find('5354') != -1: return 'STMicro' # Feitian D11CR https://docs.google.com/spreadsheets/d/10s3dA_qGvWMajv8RhCWa00x-h-1Dx6SdF9rEKN-1RIg/edit#gid=952816161
    if icfab.find('0004') != -1: return 'Philips' # Philips P8WE5032, https://www.commoncriteriaportal.org/files/epfiles/2000_06.pdf

    #if icfab == '4220': return '?? Palmera V3'

    return 'unknown (' + icfab + ')'

'''
Returns human-readable name of card vendor based on provided card name 
@param cardname: string with card name, e.g., 'NXP JCOP J2A080 80K'
@returns human-readable vendor string
'''
def get_vendor_name(cardname):
    if cardname.find('Feitian') != -1: return 'Feitian', None
    if cardname.find('G+D') != -1: return 'G&D', None
    if cardname.find('Oberthur') != -1: return 'Oberthur', 'Idemia'
    if cardname.find('Idemia') != -1: return 'Idemia', None
    if cardname.find('Gemalto') != -1: return 'Gemalto', None
    if cardname.find('Gemplus') != -1: return 'Gemplus', 'Gemalto'
    if cardname.find('Athena') != -1: return 'Athena', 'NXP'
    if cardname.find('Axalto') != -1: return 'Axalto', 'Gemalto'
    if cardname.find('Cyberflex') != -1: return 'Schlumberger', 'Gemalto'
    if cardname.find('Taisys') != -1: return 'Taisys', None
    if cardname.find('Athena') != -1: return 'Athena', 'NXP'
    if cardname.find('Fidesmo') != -1: return 'Fidesmo', None
    if cardname.find('Infineon') != -1: return 'Infineon', None
    if cardname.find('NXP') != -1: return 'NXP', None

    return 'unknown vendor', None

'''
Returns human-readable name of operating system based on provided OperatingSystemID 
@param os_id: string realization of 2 bytes OperatingSystemID (in hexadecimal, e.g., '1291')
@returns human-readable OS string
'''
def get_os_name(os_id):
    os_id = os_id.lower()
    if os_id.find('0000') != -1: return '(not provided)'
    if os_id.find('ffff') != -1: return '(not provided)'
    if os_id.find('0011') != -1: return 'Schlumberger'
    if os_id.find('0027') != -1: return 'STM027'
    if os_id.find('0230') != -1: return 'G230'
    if os_id.find('1291') != -1: return 'Gemplus/Gemalto TOP'
    if os_id.find('1671') != -1: return 'G&D Sm@rtCafe'
    if os_id.find('1981') != -1: return 'Schlumberger'
    if os_id.find('2041') != -1: return 'Axalto'
    if os_id.find('3231') != -1: return 'Gemplus TOP'
    if os_id.find('4041') != -1: return 'Oberthur OCS'
    if os_id.find('4051') != -1: return 'IBM JCOP2'
    if os_id.find('4070') != -1: return 'JCOP ?'
    if os_id.find('4091') != -1: return 'Trusted Logic jTOP'
    if os_id.find('4700') != -1: return 'NXP JCOP3&4'
    if os_id.find('4791') != -1: return 'NXP JCOP2'
    if os_id.find('4A5A') != -1: return 'JCOP ?'
    if os_id.find('544c') != -1: return 'Trusted Logic jTOP'
    if os_id.find('8211') != -1: return 'Athena SCS OS'
    if os_id.find('8231') != -1: return 'Oberthur OCS'
    if os_id.find('86aa') != -1: return 'JavaCOS'
    if os_id.find('a006') != -1: return 'G&D Sm@rtCafe'
    if os_id.find('d000') != -1: return 'Gemalto OS'
    if os_id.find('d001') != -1: return 'G&D Sm@rtCafe 7'
    
    return ''


'''
Returns human-readable name of operating system based on provided OperatingSystemID and OperatingSystemReleaseDate 
@param os_id: string realization of 2 bytes OperatingSystemID (in hexadecimal, e.g., '1291')
@param os_date: string realization of 2 bytes OperatingSystemReleaseDate (in hexadecimal, e.g., '6138')
@returns human-readable os string with release date (if known)
'''
def get_osiddate_name(os_id, os_date):
    if os_id.find('4051') != -1: 
        if os_date.find('5158') != -1:return 'JCOP 2.2 (2005)'
        if os_date.find('6138') != -1:return 'JCOP 2.2.1 (2006)'
        if os_date.find('6345') != -1:return 'JCOP 2.3.1 (2006)'
        if os_date.find('7095') != -1:return 'JCOP 2.3.1R? (2007)'
    if os_id.find('4700') != -1:
        if os_date.find('0000') != -1:return 'JCOP4 (2018)'
        if os_date.find('e4d8') != -1:return 'JCOP3 (2015)'
    if os_id.find('4791') != -1: 
        if os_date.find('7351') != -1:return 'JCOP 2.3.2 (2007)'
        if os_date.find('8102') != -1:return 'JCOP 2.?.? (2008)'
        if os_date.find('0078') != -1:return 'JCOP 2.4.1 (2010)'
        if os_date.find('2081') != -1:return 'JCOP 2.4.2R2 (2012)'
        if os_date.find('2348') != -1:return 'JCOP 2.4.2R3 (2012)'
    if os_id.find('1671') != -1:
        if os_date.find('7354') != -1: return 'G&D Sm@rtCafe (2007)'
        if os_date.find('8197') != -1: return 'G&D Sm@rtCafe (2008)'
        if os_date.find('1146') != -1: return 'G&D Sm@rtCafe (2011)'
    if os_id.find('d001') != -1:
        if os_date.find('4021') != -1: return 'G&D Sm@rtCafe (2014)'
        if os_date.find('4212') != -1: return 'G&D Sm@rtCafe (2014)'
    if os_id.find('a006') != -1:
        if os_date.find('3311') != -1: return 'G&D Sm@rtCafe (2003)'
    if os_id.find('8211') != -1:
        if os_date.find('0352') != -1:return 'Athena (2010)'
        if os_date.find('6351') != -1:return 'Athena/JCOP3 (2016)'
    if os_id.find('544c') != -1:
        if os_date.find('2151') != -1: return 'Trusted Logic jTOP (2012)'
    if os_id.find('4091') != -1:
        if os_date.find('2013') != -1: return 'Trusted Logic jTOP (2012)'
        if os_date.find('3234') != -1: return 'Trusted Logic jTOP (2003)'
    if os_id.find('86aa') != -1:
        if os_date.find('6028') != -1: return 'JavaCOS (2016)'
        if os_date.find('6083') != -1: return 'JavaCOS (2016)'
        if os_date.find('6153') != -1: return 'JavaCOS (2016)'
        if os_date.find('6188') != -1: return 'JavaCOS (2016)'
        if os_date.find('7311') != -1: return 'JavaCOS (2017)'
    if os_id.find('1291') != -1:
        if os_date.find('3349') != -1: return 'Gemplus TOP (2003)'
        if os_date.find('0356') != -1: return 'Gemplus TOP (2000)'
        if os_date.find('1102') != -1: return 'Gemalto TOP (2011)'
        if os_date.find('6095') != -1: return 'Gemplus TOP (2006)'
        if os_date.find('5181') != -1: return 'Gemplus TOP (2005)'
        if os_date.find('4214') != -1: return 'Gemplus TOP (2004)'
    if os_id.find('3231') != -1:
        if os_date.find('0300') != -1: return 'Gemplus TOP (2000)'
    if os_id.find('8231') != -1:
        if os_date.find('5343') != -1: return 'Oberthur OCS (2015)'
        if os_date.find('8150') != -1: return 'Oberthur OCS (2008)'
    if os_id.find('4041') != -1:
        if os_date.find('4091') != -1: return 'Oberthur OCS (2004)'
        if os_date.find('5273') != -1: return 'Oberthur OCS (2005)'
    if os_id.find('0011') != -1:
        if os_date.find('5273') != -1: return 'Schlumberger (2000)'
    if os_id.find('2041') != -1:
        if os_date.find('5314') != -1: return 'Axalto (2005)'
    if os_id.find('1981') != -1:
        if os_date.find('3052') != -1: return 'Schlumberger (2003)'

    return ''

def get_ictype_name(icfab, ictype):
    icfab = icfab.lower()
    ictype = ictype.lower()
    if icfab.find('0003') != -1:
        if ictype.find('0307') != -1: return 'Renesas AE46C1'   # https://csrc.nist.gov/csrc/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp2327.pdf
    if icfab.find('0004') != -1:
        if ictype.find('0015') != -1: return 'Philips P8WE5032'  # Philips P8WE5032, https://www.commoncriteriaportal.org/files/epfiles/2000_06.pdf
    if icfab.find('0005') != -1:
        if ictype.find('0045') != -1: return 'Infineon M7892 B11'   # https://csrc.nist.gov/csrc/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp2327.pdf
    if icfab.find('0005') != -1:
        if ictype.find('0045') != -1: return 'Infineon M7892 B11'   # https://csrc.nist.gov/csrc/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp2327.pdf
    if icfab.find('008c') != -1:
        if ictype.find('0089') != -1: return 'Tongxin THD89'   # https://www.commoncriteriaportal.org/files/epfiles/2017-28%20INF-2492.pdf
    if icfab.find('4070') != -1:
        if ictype.find('5072') != -1: return 'NXP P5CD144'   # https://csrc.nist.gov/csrc/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp2774.pdf
    if icfab.find('5354') != -1:
        if ictype.find('0033') != -1: return 'STM ST31 ARM'  # Feitian D11CR https://docs.google.com/spreadsheets/d/10s3dA_qGvWMajv8RhCWa00x-h-1Dx6SdF9rEKN-1RIg/edit#gid=952816161
    if icfab.find('4180') != -1:
        if ictype.find('0106') != -1: return 'Atmel AT90SC25672RCT'  # https://csrc.nist.gov/CSRC/media/projects/cryptographic-module-validation-program/documents/security-policies/140sp925.pdf
    return ''
'''
Returns randomly selected color out of defined
@returns color string
'''    
def get_random_color():
    colors = ['green','red', 'blue', 'magenta', 'brown', 'darkgreen', 'black', 'gray', 'gold', 'chocolate', 'darkorange1', 'deeppink1', 'cadetblue']
    return colors[random.randint(0, len(colors) - 1)]
    
'''
Returns randomly selected edge style
@returns edge style string
'''    
def get_random_edge_style():
    edge_styles = ['solid', 'dashed', 'bold']
    return edge_styles[random.randint(0, len(edge_styles) - 1)]

'''
Recursively reads files with CPLC info from the provided directory. 
@param walk_dir: directory with stored results for cards
@param files_with_cplc: list which will contain hash maps for the cards with CPLC defined
@param files_without_cplc: list of card names without the CPLC info
'''
def process_jcalgtest_files(walk_dir, files_with_cplc, files_without_cplc):
    files = []
    print(json.dumps(list(search_files(walk_dir)), indent=2))
    
    for filename in search_files(walk_dir):
        if not os.path.isfile(filename):
            continue
            
        files.append(filename)
        print(filename)
        
        with open(filename) as f:
            values = {}
            has_cplc = False
            for line in f:
                items = line.split(':')
                if len(items) < 2:
                    items = line.split(';')
                    if len(items) < 2:
                        continue
                
                if items[0].find('ICFabricator') > -1:
                    has_cplc = True
                
                items[0] = items[0].replace('CPLC.', '')
                
                values[items[0]] = items[1].strip()    
                
            filenameshort = path_leaf(filename)
            if has_cplc: 
                pos = filenameshort.find('ALGSUPPORT')
                if pos == -1:
                    pos = filenameshort.find('_3b')
                if pos == -1:
                    pos = filenameshort.find('_3B')
                
                
                values['CardName'] = filenameshort[:pos]
                values['CardName'] = values['CardName'].replace('_', ' ')
                files_with_cplc[filename] =  values
            else:
                files_without_cplc.append(filenameshort)
                            
#%%

# Pick suitable seed so that different lines in graph are rendered with different colors/types (needs manual testing)
random.seed(10)

""" 
Visualize CPLC information from the list of cards 
@param cplc_list: list of hash maps (with CPLC metadata) for the cards to process and visualize  
@param vendor_name_filter: if empty string '', then all vendors are printed, otherwise only the provided vendor is generated 
""" 
def generate_graph(cplc_list, vendor_name_filter):
    dot2 = Digraph(comment='Vendor={}, CPLC from JCAlgTest.org'.format(vendor_name_filter))
    graph_label = ''
    #graph_label = 'Vendor={}, CPLC visualization (JCAlgTest.org database)\n'.format(vendor_name_filter)
    graph_label += 'ICFabricator → ICFab_ICType → OperatingSystemID → OperatingSystemID_OSReleaseDate → OSReleaseLevel → CardName → Original vendor → Current vendor\n.'
    dot2.attr('graph', label=graph_label, labelloc='t', fontsize='33')
    dot2.attr(rankdir='LR', size='8,5')

    ic_fabs_types = []  # information in CSV format
    for file_cplc in cplc_list:
        cplc_values = cplc_list[file_cplc]
    
        if 'ICFabricator' in cplc_values:
            fab = cplc_values['ICFabricator']
            ictype = cplc_values['ICType']
            os_id = cplc_values['OperatingSystemID']
            os_date = cplc_values['OperatingSystemReleaseDate']
            os_level = cplc_values['OperatingSystemReleaseLevel']
            cardname = cplc_values['CardName']

            if fab == '0000' and os_id == '0000' or fab == 'ffff' and os_id == 'ffff':
                continue

            if len(fab) > 2:
                # Prepare CSV entry
                ic_fabs_types.append('{}:{}:{}:{}:  {}'.format(fab, ictype, os_id, os_date, cardname))
                
                # Prepare .dot source 
                dotfab = ' ICFab_' + fab
                dottype = ' ICType_' + ictype
                dotosid = ' OSID_' + os_id
                dotosdate = ' OSDate_' + os_date
                dotosdatelevel = dotosdate + ' OSLevel_' + os_level

                if get_vendor_name(cardname)[1] == None:
                    vendor_name_temp = get_vendor_name(cardname)[0]
                else:
                    vendor_name_temp = get_vendor_name(cardname)[1]

                if vendor_name_filter == '' or vendor_name_filter == vendor_name_temp:
                    #ICFab_ICType -> OSID_OSDate -> CardName -> Vendor
                    dot2.attr('node', color='lightgray')
                    dot2.attr('node', style='filled')
                    dot2.attr('node', fontsize='20')
                    dot2.node(get_fab_name(fab))
                    vendor, vendor_current = get_vendor_name(cardname)
                    if vendor_current is not None:
                        vendor_name_curr = '\'' + vendor_current + '\''
                        vendor_name = 'v=' + vendor
                    else:
                        vendor_name = 'v=' + vendor
                        vendor_name_curr = '\'' + vendor + '\''

                    dot2.node(vendor_name)
                    dot2.attr('node', fontsize='30')
                    dot2.node(vendor_name_curr)
                    dot2.attr('node', fontsize='20')
                    dot2.node(dotosid, dotosid + '\n' + get_os_name(os_id))
                    dot2.attr('node', color='lightgray')
                    dot2.attr('node', style='solid')
                    dot2.attr('node', fontsize='14')
                    dot2.node(dotfab+dottype, '{}\n{}'.format(dotfab+dottype, get_ictype_name(fab, ictype)))
                    dot2.node(dotosid+dotosdate, dotosid+dotosdate + '\n' + get_osiddate_name(os_id, os_date))
                    dot2.node(dotosdatelevel, os_level)
                    dot2.node(cardname)
                    
                    dot2.attr('edge', color='lightgray')
                    # assign connection line color and type correctly to allow for tracking the 
                    rndcolor = get_random_color()
                    rndedgestyle = get_random_edge_style()
                    dot2.edge(get_fab_name(fab), dotfab+dottype, color=rndcolor, style=rndedgestyle)
                    dot2.edge(dotfab+dottype, dotosid, color=rndcolor, style=rndedgestyle)
                    dot2.edge(dotosid, dotosid+dotosdate, color=rndcolor, style=rndedgestyle)
                    dot2.edge(dotosid+dotosdate, dotosdatelevel, color=rndcolor, style=rndedgestyle)
                    dot2.edge(dotosdatelevel, cardname, color=rndcolor, style=rndedgestyle)
                    #dot2.edge(dotosid+dotosdate, cardname, color=rndcolor, style=rndedgestyle)
                    dot2.edge(cardname, vendor_name, color=rndcolor, style=rndedgestyle)
                    if vendor_name != vendor_name_curr:
                        dot2.edge(vendor_name, vendor_name_curr, color=rndcolor, style=rndedgestyle)

    # Generate dot graph using GraphViz into pdf 
    vendor_name_filter = vendor_name_filter.replace('/', '_')
    dot2.render('test-output/cplc_{}'.format(vendor_name_filter), view=True)

    # Print CSV formated lines
    ic_fabs_types.sort()
    for pair in ic_fabs_types:
        print(pair)


def render_all_vendors():
    files_with_cplc = {}
    files_without_cplc = []
    
    walk_dir = '..\\Profiles\\results\\'
    process_jcalgtest_files(walk_dir, files_with_cplc, files_without_cplc)
    

    vendors = ['', 'NXP', 'Infineon', 'Gemalto', 'Feitian', 'G&D', 'Idemia']
    #vendors = ['']
    #vendors = ['G&D']
    for vendor in vendors:
        generate_graph(files_with_cplc, vendor)


    print('Cards with CPLC: {}'.format(len(files_with_cplc)))
    print('Cards without CPLC: {}'.format(len(files_without_cplc)))

def main():
    render_all_vendors()

if __name__ == "__main__":
    main()
