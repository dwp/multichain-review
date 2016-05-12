#DLT: GOVERNMENT INTERDEPARTMENTAL MULTICHAIN
May 2016  
BY STEVEN BAKHTIARI, MATT SMITH

---

##Summary
MultiChain, a fork of Bitcoin Core, the reference client for the Bitcoin network, is a permissioned blockchain enabling organisations to quickly configure and run a distributed ledger capable of managing digital and native assets. This experiment aims to determine the possibility of using MultiChain to run a government-maintained interdepartmental ledger that could provide value to multiple departments needing access to the same data.

##Key insights
MultiChain offers a number of features, making it a platform worthy of consideration.

* Based on open source software, and soon to be released under a GPLv3 licence. It's worth noting that it is still in alpha release.
* Fully permissioned, offering the ability to deploy a private, fully access controlled ledger. MultiChain allows organisations to easily assign different permissions/rights to nodes that are given access to the blockchain.
* Doesn't rely on Proof of Work as a security mechanism, and instead provides a means of configuring a round-robin selection to prevent nodes from adding a sequential set of blocks to the chain.
* Supports atomic transactions between multiple participants using partial transactions.
* Forked from bitcoin-core, MultiChain provides a JSON-RPC API to interact with nodes, and shares many of the same features as Bitcoin.
* Enables storage of arbitrary data on the blockchain. 
* The introduction of blockchain technology will provide value most when there are many actors that need to read and write to the system. One example could be HMRC assigning tax liability, DWP assigning benefit liability and then having those tokens returned to HMRC and the DWP once the liability is fulfilled.

##Nature of research in this pack
The insight from this experiment may prove the usefulness of investment in the technology in the medium term, in addition to bigger, wider reaching projects in the long term. It is worth noting that distributed ledger technology is still young, and as such, improvements and new use cases surface regularly.

##Problem
Government departments are often required to exchange information, where frequently, there may be dependencies on the data, impacting day to day operations or decisions being made. Traditional forms of data exchange may carry overheads and the potential for error and miscommunication.

##Hypothesis
A MultiChain blockchain can be created to facilitate the sharing of data, and improving security all while reducing error, fraud, and reducing the risks and costs associated with the currently employed means of data sharing. Having access to shared data will allow government departments to more easily conduct day to day operations and make decisions.

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
This experiment makes use of a cluster configuration provided by software agency, Kunstmaan. Some minor tweaks have been made to accommodate the experiment, and the version supplied with this research pack should be used if following these steps.

To start the cluster, navigate to the directory containing the `docker-compose.yml` file and run `docker-compose`.

```
$ cd ./docker-multichain
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
$ docker exec -it <container-id> bash
root@<container-id>:/# multichain-cli govchain help
```

###Testing the permissions system
While it is possible to deploy MultiChain as a completely open blockchain (it may be configured to allow any node to connect, make payments, receive payments, issue assets, etc), its permissions system allows one to configure roles on a per-address basis.

The provided Docker Composer configuration, when run, starts multiple containers, one for each node. This configuration will be used to prove the utility of the permissions system. However, as MultiChain does not allow the core parameters of a chain to be modified once it has been established, for the purposes of demonstrating to those following along with the provided Docker packages, a new chain should be created. Executing a shell instance on one of the nodes, use the `multichain-util` command to generate the chain:

```
$ docker exec -it <container-id> bash
root@<container-id>:/# multichain-util create testchain
```

A new set of files should have been generated and placed in the directory `/root/.multichain/testchain` containing, amongst other things, some configuration for the chain. Before running the daemon, append the following line to the testchain `multichain.conf` file to override the default port for the JSON-RPC API, then run the daemon:

```
root@<container-id>:/# echo "rpcport=8012" >> /root/.multichain/testchain/multichain.conf
root@<container-id>:/# multichaind testchain -daemon
```

Running the daemon should output some connection information, along with the command needed for other nodes to connect. Executing a shell instance on one of the other nodes, attempt to connect. 

```
$ docker exec -it <container-id> bash
root@<container-id>:/# multichaind testchain@<ip>:<port>
```

The output should indicate that the node attempting to connect does not have the relevant permissions. The output also contains example commands which can be used to set permissions, and includes the relevant address to use when doing so. Returning to the first node, connect permissions may be granted:

```
root@<container-id>:/# multichain-cli testchain grant <address> connect
```

Attempting to connect again from the other node should now work as expected. First, the JSON-RPC API port number must be overridden, then the connection can be made:

```
root@<container-id>:/# echo "rpcport=8013" >> /root/.multichain/testchain/multichain.conf
root@<container-id>:/# multichaind testchain@<ip>:<port> -daemon
root@<container-id>:/# multichain-cli testchain getinfo
```

##Findings
###Security and permissions
MultiChain enables organisations to deploy private blockchains with fully configurable permissions, making it well suited for use by multiple government departments with requirements to share data, while each managing their own IT infrastructure with varying budgets.

Permissions may be assigned to nodes covering an array of features including:

* Connecting
* Issuing assets
* Sending assets
* Receiving assets
* Mining assets
* Administering the blockchain

Public-key cryptography is used extensively in crytocurrencies to verify identities, particularly when sending and receiving data between participants in a transaction. In addition to this, MultiChain uses the same mechanism during the connection handshake when one node attempts to connect to another, ensuring that any node wishing to participate in the network is both known to others by its public key, and is also in possession of the corresponding private key, which is used to return a digital signature of a challenge message received during the connection attempt. 

Such fine grained permissions simplify the process of defining different rights, or entirely limiting system access to specific actors, while features of the blockchain itself make it suitable for helping government to improve record keeping and the sharing of data across departments. 

In the example scenario described in this research pack, a set of nodes could be managed by the Home Office, holding addresses for each individual in the country. Another set of nodes could be managed by the DWP which records any benefit payments made to those individuals. Likewise, HMRC would record tax liability and tax payments received for said individuals. Where the DWP determine entitlement based on national insurance contributions, or where HMRC adjust tax liability based on benefits paid, the information is all available on the network without each department having to explicitly compile and send the data to each another.

![](./assets/gov-blockchain.png)

All of these nodes combined would form a single government blockchain with multiple writers and readers accessing the information.

###The value of mining on a private blockchain
In public blockchain implementations, it is necessary to enforce diversity in the pool of nodes responsible for building blocks of transactions and adding them to the chain. There are numerous approaches for doing this, the most common of which, used by the Bitcoin blockchain, is known as Proof of Work (PoW). This works by requiring nodes to solve a cryptographic "puzzle", in simple terms, by repeatedly peforming a hash of the candidate block's contents until the resulting hash meets a given criteria. This process is time consuming, computationally expensive, and consequently, costly in real world terms. It is this process that is typically referred to as mining.

In a private blockchain, where the actors are known and authorised, the requirement to perform computationally expensive work in order to build blocks becomes unnecessary; other means of enforcing diversity may be employed. The current version of BlockChain still maintains a Bitcoin style PoW, purely for the purposes of regulating the rate of block production, so in order to enforce diversity, the system uses a randomised round-robin with a configurable diversity value. This value represents the proportion of nodes that would need to collude in order to undermind the network, and so, depending on the value, prevents a node from submitting a new block for inclusion in the chain if it had previously built one of the last *n* blocks.

###Multiple addresses
Privacy through anonymity is one of the primary motivations and driving forces behind the development of many open blockchains. The recommendation for users of Bitcoin, for example, is to generate a new private and public keypair (a new address) for each new payment to be received. 

MultiChain allows for the easy creation of multiple addresses per node, and also provides API functions to be able to send and receive assets to and from specific addresses. This, along with the ability to assign permissions on a per-address basis, essentially opens the door to treating addresses as accounts, and fits well with the concept of having an address for each individual being represented in the system i.e. one address for each person in the country.

###Atomic exchange transactions
Because MultiChain provides the ability to include multiple native assets in one blockchain, as opposed to a single one like the Bitcoin Blockchain, it has added an exchange transaction which swaps some of one asset for some of another. This transaction will be atomic, meaning that the two-way exchange must succeed or fail as a whole (also known as delivery-versus-payment).

Unlike one way transactions, which need only be signed by the sender, an atomic transaction must be signed by all participants involved in the exchange. MultiChain does not currently provide a way to communicate partial transactions over the Blockchain, requiring participants to perform this off-chain.

The process is as follows: a transaction is created identifying a set of outputs and inputs (e.g. Alice creates a transactions stating that £10 will be spent and $15 should be received). This is digitally signed, creating a 'partial transaction', which is then transferred to another participant who can then add their own inputs and outputs, before signing the transaction too. Provided all of the inputs and outputs are balanced, the transaction can then be confirmed on the blockchain.