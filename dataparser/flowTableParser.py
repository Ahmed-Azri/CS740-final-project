from pyparsing import *

varname = Word(alphas+'_')
value = Word(printables)
assignment = varname.setResultsName('vname')\
        + '=' + value.setResultsName('val')

def parse_line(line):
    row = {}
    for tokens,start,end in assignment.scanString(line.replace(',', ' ')):
        # print tokens.dump()
        row[tokens.vname] = tokens.val
    return row

def text2table(linelist):
    table = []
    for line in linelist:
        if not line.strip().startswith('cookie='):
            continue
        d = parse_line(line)
        clean_value(d)
        table.append(d)

    return table

def clean_value(d):
    try:
        d['duration'] = d['duration'].strip('s')
    except KeyError:
        pass

# line = "cookie=0x20000000000000, duration=147.156s, table=0, n_packets=146, n_bytes=14308, idle_timeout=5, idle_age=0, priority=1,ip,in_port=2,dl_src=02:a9:f3:97:09:a0,dl_dst=02:47:38:b8:cc:2a,nw_src=10.10.1.2,nw_dst=10.10.1.1 actions=output:1"

# # f = open('./zihao.log', 'r')
# f = open('./openflowdump-sample.log', 'r')
# print text2table(f.readlines())

# exit(0)

def text2table_old(linelist):
    num = Word(nums)
    actionname = Word(printables)
    octnum = Word("0x"+hexnums)
    realnum = Word(nums+".").setParseAction( lambda tokens: float(tokens[0]) )
    intnum = Word(nums).setParseAction( lambda tokens: int(tokens[0]) )

    row = "cookie="+octnum.setResultsName('cookie')+','\
            +'duration='+realnum.setResultsName('duration')+'s,'\
            +'table='+intnum.setResultsName('table')+','\
            +'n_packets='+intnum.setResultsName('n_packets')+','\
            +'n_bytes='+intnum.setResultsName('n_bytes')+','\
            +'idle_age='+intnum.setResultsName('idle_age') +','\
            +'priority='+intnum.setResultsName('priority') \
            +'actions='+actionname.setResultsName('actions')

    table = []
    for line in linelist:
        if not line.startswith(' cookie='):
            continue
        rowresult = row.parseString(line)
        # print rowresult.dump()
        table.append( dict(rowresult) )

    return table

# f = open('./openflowdump-sample.log', 'r')
# text = f.readlines()

# tab = text2table(text)
# pprint.pprint( tab )

