#DLT: GOVERNMENT INTERDEPARTMENTAL MULTICHAIN
May 2016  
BY STEVEN BAKHTIARI, MATT SMITH

---

##Summary
MultiChain, a fork of Bitcoin Core, the reference client for the Bitcoin network, is a permissioned blockchain enabling organisations to quickly configure and run a distributed ledger capable of managing digital and native assets. This experiment aims to determine the possibility of using MultiChain to run a government-maintained interdepartmental ledger that could provide value to multiple departments needing access to the same data.

##Type of deliverable
**Experiment**.

##Nature of research in this pack
The insight from this experiment may prove the usefulness of investment in the technology in the medium term, in addition to bigger, wider reaching projects in the long term. It is worth noting that distributed ledger technology is still young, and as such, improvements and new use cases surface regularly.

##Problem
Various taxes along with national insurance contributions are collected by HMRC, while a variety of benefit payments are distributed by the DWP. When calculating a claimant's entitlement, some benefits take account of an individual's national insurance contributions, and on the inverse, some forms of benefit are liable for tax. At present, HMRC and the DWP exchange information by using copies of spreadsheets and databases. This form of data exchange carries overheads and the potential for error and miscommunication.

##Hypothesis
A MultiChain blockchain can be created to facilitate the sharing of data, improve security while reducing error, fraud, and the reducing the risks and costs associated with the currently employed means of data sharing.  MultiChain offers the following features, making it a platform worthy of consideration:

* Based on open source software, and soon to be released under a GPLv3 licence
* Fully permissioned, offering the ability to deploy a private, fully access controlled ledger
* Uses a randomised round robin approach to forming blocks, rather than relying on proof of work
* Supports atomic transactions between multiple parties without the need to go off-network
* Provides a JSON-RPC API to interact with nodes on the network
* Enables storage of arbitrary data on the blockchain

##Experiment and research proposed
Run a Docker MultiChain cluster where each node represents a government department capable of reading and writing data that is made immediately available to other departments.

##Work undertaken
In order to attain the knowledge to produce the insight in this research pack, a solid understanding of the technology was required. For those wishing to independently evaluate MultiChain, the steps taken to run it for this experiment are detailed below.

###Prerequisites
To get started, ensure that both `docker` and `docker-compose` are installed. If attempting to run the container on a Mac or Windows computer, `docker-machine` will also be required. It may also be worth creating a new virtual machine to house the MultiChain containers:

```
$ docker-machine create --driver virtualbox multichain-experiments       
$ docker-machine env multichain-experiments          
$ eval $(docker-machine env multichain-experiments)          
```

###Launching the cluster
This experiment makes use of a cluster configuration provided by software agency, Kunstmaan. Some minor tweaks have been made to accomodate the experiment, and the version supplied with this research pack should be used if following these steps.

To start the cluster, navigate to the directory containing the `docker-compose.yml` file and run `docker-compose`.

```
$ cd ~/path/to/docker-files
$ docker-compose up
```
After a minute or two, all of the nodes should be up and running.

###Interacting with the cluster
If running docker on a Mac or Windows computer, it is necessary to find the IP address of the virtual machine in order to interact with the API or access the explorer.

```
$ docker-machine ip multichain-experiments
```

With the IP address, it should now be possible to view the MultiChain explorer by visiting `http://<vm-ip-address>:2750` in a web browser. Curl requests, testing access to the API, may also be performed in order to verify that the cluster is running:

```
$ curl -i -X POST -d '{"method":"getinfo"}' http://multichainrpc:password@<vm-ip-address>:8081
$ curl -i -X POST -d '{"method":"getinfo"}' http://multichainrpc:password@<vm-ip-address>:8082
```

Note the two different port numbers in the requests above. The Docker Composer configuration forwards these ports to two different containers, each running a MultiChain node.

In addition to making requests directly to the API, MultiChain provides a command line interface to the API. This makes for easy testing and interaction during evaluation. In order to use this, one must ascertain a container ID (in this instance, two containers, master and node) by running `docker ps` and then `docker exec` to run a new shell instance. A bash prompt will now be available, providing access to MultiChain's command line interface:

```
$ docker ps
$ docker exec -i -t <container-id> bash
root@<container-id>:/# multichain-cli govchain help
```

##Findings

###Security and permissions
MultiChain enables organisations to deploy private, permissioned, fully configurable blockchains. This makes it a solution worthy of consideration when considering a multi-master distributed system to be used by multiple government departments, each managing their own IT infrastructure, with varying budgets, and with different requirements on the data being stored. For instance, some departments may only need occasional read only access for oversight or reporting, while others will need to write. 

Permissions may be assigned to nodes covering an array of features including:

* Connecting to the other nodes
* Issuing assets
* Sending assets
* Receiving assets
* Mining assets
* Administering the network

Public-key cryptography is used extensively in crytocurrencies and blockchain implementations to manage identities, and the sending and receiving of data between participants. In addition to this, MultiChain also uses cryptography during the handshake when a node attempts to connect to another, ensuring that any node wishing to participate is both known to others by a public key, and that the node is in possession of the private key associated with the public key.

Such fine grained permissions simplify the process of defining different rights, or entirely limiting system access to specific actors, while features of the blockchain itself make it suitable for helping government to improve record keeping and the sharing of data across departments. 

In the example scenario described in this research pack, a set of nodes could be managed by the Home Office, holding addresses for each individual in the country. Another set of nodes could be managed by the DWP which records any benefit payments made to those individuals. Likewise, HMRC would record tax liability and tax payments received for said individuals. Where the DWP determine entitlement based on national insurance contributions, or where HMRC adjust tax liability based on benefits paid, the information is all available on the network without each department having to explicitly compile and send the data to each another.

###The value of mining on a private blockchain
In public blockchain implementations, it is necessary to enforce diversity in the pool of nodes responsible for building blocks of transactions and adding them to the chain. There are numerous approaches for doing this, the most common of which, used by the Bitcoin blockchain, is known as Proof of Work (PoW). This works by requiring nodes to solve a cryptographic "puzzle", in simple terms, by repeatedly peforming a hash of the candidate block's contents until the resulting hash meets a given criteria. This process is time consuming, computationally expensive, and consequently, costly in real world terms. It is this process that is typically referred to as mining.

In a private blockchain, where the actors are known and authorised, the requirement to perform computationally expensive work in order to build blocks becomes unnecessary; other means of enforcing diversity may be employed. The current version of BlockChain still maintains a Bitcoin style PoW, purely for the purposes of regulating the rate of block production, so in order to enforce diversity, the system uses a randomised round-robin with a configurable diversity value. This value represents the proportion of nodes that would need to collude in order to undermind the network, and so, depending on the value, prevents a node from submitting a new block for inclusion in the chain if it had previously built one of the last *n* blocks.

###Atomic exchange transactions


##Innovation radar update


##Innovation gallery