# libraries
library(ggplot2)
library(plyr)
library(reshape2)

# copy the following so you can do sme()
# WORKDIRECTORY='/u/j/h/jhe/workdir/cs740proj'
WORKDIRECTORY='/Users/junhe/BoxSync/workdir/cs740proj'
THISFILE     ='analyzer.r'
setwd(WORKDIRECTORY)
sme <- function()
{
    setwd(WORKDIRECTORY)
    source(THISFILE)
}

explore.1 <- function()
{
    transfer <- function()
    {
    }
    load <- function(fpath)
    {
        #d = read.csv('./benchmark_read_128_files_1KB.txt.parsed', header=T)
        d = read.csv(fpath, header=T)
        #d = read.csv('./try.parsed', header=T)
        #print(head(d, 50))
        return(d)
    }

    clean <- function(d)
    {
        return(d)
    }

    func <- function(d)
    {
        rename_for_human <- function(ip) 
        {
            hostname = revalue(ip, c(
                    "10.10.1.8" = "Client" ,
                    "10.10.1.9" = "Namenode" ,
                    "10.10.1.1" = "Datanode1" ,
                    "10.10.1.2" = "Datanode2" ,
                    "10.10.1.3" = "Datanode3" ,
                    "10.10.1.4" = "Datanode4" ,
                    "10.10.1.15" = "Datanode5" ,
                    "10.10.1.16" = "Datanode6" ,
                    "10.10.1.17" = "Datanode7"))
            return (hostname)
        }
        cbPalette <- c("#89C5DA", "#DA5724", "#74D944", "#CE50CA", "#3F4921", "#C0717C", "#CBD588", "#5F7FC7", 
                "#673770", "#D3D93E", "#38333E", "#508578", "#D7C1B1", "#689030", "#AD6F3B", "#CD9BCD", 
                "#D14285", "#6DDE88", "#652926", "#7FDCC0", "#C84248", "#8569D5", "#5E738F", "#D1A33D", 
                "#8A7C64", "#599861")

        # n_packets,idle_age,nw_dst,dl_src,actions,idle_timeout,cookie,tp_src,duration,table,nw_src,n_bytes,priority,tp_dst,dl_dst,in_port
        tmp = levels(d$nw_src)
        levels(d$nw_src) = rename_for_human(tmp) 
        tmp = levels(d$nw_dst)
        levels(d$nw_dst) = rename_for_human(tmp) 

        # d$flow = with(d, paste(nw_dst,dl_src, tp_src, nw_src, tp_dst, dl_dst, in_port, sep=':'))
        # d$flow = interaction(d$nw_src, d$tp_src, d$nw_dst, d$tp_dst, sep=':')
        # d$flow = paste(d$nw_src, d$tp_src, d$nw_dst, d$tp_dst, sep=':')
        d$flow = paste(d$nw_src, d$nw_dst, sep='->')

        # d = subset(d, flow == 'Datanode7->Client')
        d = subset(d, flow != '->')
        # d$flow = paste(d$nw_src, d$tp_src, d$nw_dst, d$tp_dst, sep=':')

        d = aggregate(n_bytes~batchid+flow, data=d, sum)

        d$pre_bytes = c(d$n_bytes[1], d$n_bytes[-length(d$n_bytes)])
        d$bw = d$n_bytes - d$pre_bytes
        d$bw = ifelse(d$bw > 0, d$bw, 0)

        # p <- ggplot(d, aes(x=batchid, y=n_bytes/1024)) +
        p <- ggplot(d, aes(x=batchid, y=bw/1024)) +
            geom_point() +
            # geom_line() +
            # scale_color_manual(values=cbPalette) +
            facet_wrap(~flow) +
            xlab('second') +
            ylab('bandwidth (kb/s)')
            theme_bw()
        print(p)
    }

    do_main <- function()
    {
        # dir = './data/'
        dir = './benchmark4/'
        files = list.files(path=dir, pattern="*parsed")
        # files = list.files(path=dir, pattern="replicate_read_256_files_10MB.txt.parsed")
        print(files)
        for (f in files) {
                if (! f %in% c("replicate_read_64_files_1KB.txt.parsed")) {
                    next
                }
                print(f)
                d = load(paste(dir,f,sep=''))
                d = clean(d)
                func(d)
                r = readline()
                if (r == 'a') {
                        return()
                }
        }
    }
    do_main()
}

explore.2 <- function()
{
    transfer <- function()
    {
    }
    load <- function(fpath)
    {
        #d = read.csv('./benchmark_read_128_files_1KB.txt.parsed', header=T)
        d = read.csv(fpath, header=T)
        #d = read.csv('./try.parsed', header=T)
        #print(head(d, 50))
        return(d)
    }

    clean <- function(d)
    {
        return(d)
    }

    func <- function(d)
    {
        rename_for_human <- function(ip) 
        {
            hostname = revalue(ip, c(
                    "10.10.1.8" = "Client" ,
                    "10.10.1.9" = "Namenode" ,
                    "10.10.1.1" = "Datanode1" ,
                    "10.10.1.2" = "Datanode2" ,
                    "10.10.1.3" = "Datanode3" ,
                    "10.10.1.4" = "Datanode4" ,
                    "10.10.1.15" = "Datanode5" ,
                    "10.10.1.16" = "Datanode6" ,
                    "10.10.1.17" = "Datanode7"))
            return (hostname)
        }
        cbPalette <- c("#89C5DA", "#DA5724", "#74D944", "#CE50CA", "#3F4921", "#C0717C", "#CBD588", "#5F7FC7", 
                "#673770", "#D3D93E", "#38333E", "#508578", "#D7C1B1", "#689030", "#AD6F3B", "#CD9BCD", 
                "#D14285", "#6DDE88", "#652926", "#7FDCC0", "#C84248", "#8569D5", "#5E738F", "#D1A33D", 
                "#8A7C64", "#599861")

        # n_packets,idle_age,nw_dst,dl_src,actions,idle_timeout,cookie,tp_src,duration,table,nw_src,n_bytes,priority,tp_dst,dl_dst,in_port
        tmp = levels(d$nw_src)
        levels(d$nw_src) = rename_for_human(tmp) 
        tmp = levels(d$nw_dst)
        levels(d$nw_dst) = rename_for_human(tmp) 

        # d$flow = with(d, paste(nw_dst,dl_src, tp_src, nw_src, tp_dst, dl_dst, in_port, sep=':'))
        # d$flow = interaction(d$nw_src, d$tp_src, d$nw_dst, d$tp_dst, sep=':')
        # d$flow = paste(d$nw_src, d$tp_src, d$nw_dst, d$tp_dst, sep=':')
        d$flow = paste(d$nw_src, d$nw_dst, sep='->')
        d = subset(d, flow != '->')

        # d = subset(d, flow == 'Datanode7->Client')
        # d$flow = paste(d$nw_src, d$tp_src, d$nw_dst, d$tp_dst, sep=':')

        d = aggregate(n_bytes~batchid+flow, data=d, sum)

        d$pre_bytes = c(d$n_bytes[1], d$n_bytes[-length(d$n_bytes)])
        d$bw = d$n_bytes - d$pre_bytes
        d$bw = ifelse(d$bw > 0, d$bw, 0)

        # p <- ggplot(d, aes(x=batchid, y=n_bytes/1024)) +
        p <- ggplot(d, aes(x=batchid, y=bw/1024)) +
            geom_point() +
            # geom_line() +
            # scale_color_manual(values=cbPalette) +
            facet_wrap(~flow) +
            xlab('second') +
            ylab('bandwidth (kb/s)') +
            theme_bw()
        print(p)
    }

    do_main <- function()
    {
        # dir = './data/'
        dir = './benchmark4/'
        files = list.files(path=dir, pattern="*parsed")
        # files = list.files(path=dir, pattern="replicate_read_256_files_10MB.txt.parsed")
        print(files)
        for (f in files) {
                if (! f %in% c("replicate_write_256_files_10MB.txt.parsed")) {
                    next
                }
                print(f)
                d = load(paste(dir,f,sep=''))
                d = clean(d)
                func(d)
                r = readline()
                if (r == 'a') {
                        return()
                }
        }
    }
    do_main()
}

main <- function()
{
    # explore.1()
    explore.2()
}
main()

