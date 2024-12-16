package org.web3j.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.10.3.
 */
@SuppressWarnings("rawtypes")
public class Rendezvous extends Contract {
    public static final String BINARY = "608060405234801562000010575f80fd5b50600280546001810182555f919091526040805180820190915260078152664144534230303160c81b60208201525f8051602062000c4a833981519152909101906200005d908262000151565b50600280546001810182555f9190915260408051808201909152600781526620a229a118181960c91b60208201525f8051602062000c4a83398151915290910190620000aa908262000151565b5062000219565b634e487b7160e01b5f52604160045260245ffd5b600181811c90821680620000da57607f821691505b602082108103620000f957634e487b7160e01b5f52602260045260245ffd5b50919050565b601f8211156200014c575f81815260208120601f850160051c81016020861015620001275750805b601f850160051c820191505b81811015620001485782815560010162000133565b5050505b505050565b81516001600160401b038111156200016d576200016d620000b1565b62000185816200017e8454620000c5565b84620000ff565b602080601f831160018114620001bb575f8415620001a35750858301515b5f19600386901b1c1916600185901b17855562000148565b5f85815260208120601f198616915b82811015620001eb57888601518255948401946001909101908401620001ca565b50858210156200020957878501515f19600388901b60f8161c191681555b5050505050600190811b01905550565b610a2380620002275f395ff3fe608060405234801561000f575f80fd5b5060043610610055575f3560e01c80630636b438146100595780631cf38b6a14610082578063b307b35614610097578063c8d3d80b146100c2578063e9b6fb4b146100e6575b5f80fd5b61006c6100673660046105a6565b6100f7565b6040516100799190610600565b60405180910390f35b610095610090366004610706565b61019d565b005b6100aa6100a53660046105a6565b610475565b6040516001600160a01b039091168152602001610079565b6100d56100d0366004610808565b61049d565b60405161007995949392919061082e565b600154604051908152602001610079565b60028181548110610106575f80fd5b905f5260205f20015f91509050805461011e90610873565b80601f016020809104026020016040519081016040528092919081815260200182805461014a90610873565b80156101955780601f1061016c57610100808354040283529160200191610195565b820191905f5260205f20905b81548152906001019060200180831161017857829003601f168201915b505050505081565b805182511461020c5760405162461bcd60e51b815260206004820152603060248201527f536572766963652049447320616e6420707269636573206d757374206861766560448201526f040e8d0ca40e6c2daca40d8cadccee8d60831b60648201526084015b60405180910390fd5b5f5b82518110156102c0575f83828151811061022a5761022a6108ab565b602002602001015160ff161015801561026257506002548351849083908110610255576102556108ab565b602002602001015160ff16105b6102ae5760405162461bcd60e51b815260206004820152601c60248201527f41207365727669636520494420776173206e6f7420616c6c6f776564000000006044820152606401610203565b806102b8816108bf565b91505061020e565b50335f908152602081905260409020805460ff16610319576001805480820182555f919091527fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf60180546001600160a01b031916331790555b805460ff19166001908117825581016103328582610931565b5060028101805467ffffffffffffffff878116600160801b0267ffffffffffffffff60801b198a8316600160401b026fffffffffffffffffffffffffffffffff19909416928c16929092179290921716179055610392600382015f610569565b61039f600482015f610569565b5f5b835181101561046b57816003018482815181106103c0576103c06108ab565b60209081029190910181015182546001810184555f93845292829020918304909101805460ff928316601f9094166101000a938402929093021990921617905582516004830190849083908110610419576104196108ab565b60209081029190910181015182546001810184555f93845292829020918304909101805460ff928316601f9094166101000a938402929093021990921617905580610463816108bf565b9150506103a1565b5050505050505050565b60018181548110610484575f80fd5b5f918252602090912001546001600160a01b0316905081565b5f602081905290815260409020805460018201805460ff90921692916104c290610873565b80601f01602080910402602001604051908101604052809291908181526020018280546104ee90610873565b80156105395780601f1061051057610100808354040283529160200191610539565b820191905f5260205f20905b81548152906001019060200180831161051c57829003601f168201915b5050506002909301549192505067ffffffffffffffff80821691600160401b8104821691600160801b9091041685565b5080545f8255601f0160209004905f5260205f209081019061058b919061058e565b50565b5b808211156105a2575f815560010161058f565b5090565b5f602082840312156105b6575f80fd5b5035919050565b5f81518084525f5b818110156105e1576020818501810151868301820152016105c5565b505f602082860101526020601f19601f83011685010191505092915050565b602081525f61061260208301846105bd565b9392505050565b803567ffffffffffffffff81168114610630575f80fd5b919050565b634e487b7160e01b5f52604160045260245ffd5b604051601f8201601f1916810167ffffffffffffffff8111828210171561067257610672610635565b604052919050565b5f82601f830112610689575f80fd5b8135602067ffffffffffffffff8211156106a5576106a5610635565b8160051b6106b4828201610649565b92835284810182019282810190878511156106cd575f80fd5b83870192505b848310156106fb57823560ff811681146106ec575f8081fd5b825291830191908301906106d3565b979650505050505050565b5f805f805f8060c0878903121561071b575f80fd5b61072487610619565b95506020610733818901610619565b955061074160408901610619565b9450606088013567ffffffffffffffff8082111561075d575f80fd5b818a0191508a601f830112610770575f80fd5b81358181111561078257610782610635565b610794601f8201601f19168501610649565b8181528c858386010111156107a7575f80fd5b81858501868301375f91810190940152919450608089013591808311156107cc575f80fd5b6107d88b848c0161067a565b945060a08a01359250808311156107ed575f80fd5b50506107fb89828a0161067a565b9150509295509295509295565b5f60208284031215610818575f80fd5b81356001600160a01b0381168114610612575f80fd5b851515815260a060208201525f61084860a08301876105bd565b67ffffffffffffffff9586166040840152938516606083015250921660809092019190915292915050565b600181811c9082168061088757607f821691505b6020821081036108a557634e487b7160e01b5f52602260045260245ffd5b50919050565b634e487b7160e01b5f52603260045260245ffd5b5f600182016108dc57634e487b7160e01b5f52601160045260245ffd5b5060010190565b601f82111561092c575f81815260208120601f850160051c810160208610156109095750805b601f850160051c820191505b8181101561092857828155600101610915565b5050505b505050565b815167ffffffffffffffff81111561094b5761094b610635565b61095f816109598454610873565b846108e3565b602080601f831160018114610992575f841561097b5750858301515b5f19600386901b1c1916600185901b178555610928565b5f85815260208120601f198616915b828110156109c0578886015182559484019460019091019084016109a1565b50858210156109dd57878501515f19600388901b60f8161c191681555b5050505050600190811b0190555056fea2646970667358221220284210f154dbe54b82ef8b6fcb0acf32826ae4aff7cd03cedf92670d09054c2564736f6c63430008140033405787fa12a823e0f2b7631cc41b3ba8828b3321ca811111fa75cd3aa3bb5ace";

    public static final String FUNC_ALLOWEDSERVICES = "allowedServices";

    public static final String FUNC_GETPEERARRAYSIZE = "getPeerArraySize";

    public static final String FUNC_HEDERAADDRESSTOPEER = "hederaAddressToPeer";

    public static final String FUNC_PEERLIST = "peerList";

    public static final String FUNC_PUTPEERAVAILABLESELF = "putPeerAvailableSelf";

    @Deprecated
    protected Rendezvous(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Rendezvous(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Rendezvous(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Rendezvous(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<String> allowedServices(BigInteger param0) {
        final Function function = new Function(FUNC_ALLOWEDSERVICES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> getPeerArraySize() {
        final Function function = new Function(FUNC_GETPEERARRAYSIZE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple5<Boolean, String, BigInteger, BigInteger, BigInteger>> hederaAddressToPeer(String param0) {
        final Function function = new Function(FUNC_HEDERAADDRESSTOPEER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint64>() {}, new TypeReference<Uint64>() {}, new TypeReference<Uint64>() {}));
        return new RemoteFunctionCall<Tuple5<Boolean, String, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple5<Boolean, String, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple5<Boolean, String, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<Boolean, String, BigInteger, BigInteger, BigInteger>(
                                (Boolean) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> peerList(BigInteger param0) {
        final Function function = new Function(FUNC_PEERLIST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> putPeerAvailableSelf(BigInteger stdOutTopic, BigInteger stdInTopic, BigInteger stdErrTopic, String peerID, List<BigInteger> serviceIDs, List<BigInteger> prices) {
        final Function function = new Function(
                FUNC_PUTPEERAVAILABLESELF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint64(stdOutTopic), 
                new org.web3j.abi.datatypes.generated.Uint64(stdInTopic), 
                new org.web3j.abi.datatypes.generated.Uint64(stdErrTopic), 
                new org.web3j.abi.datatypes.Utf8String(peerID), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint8>(
                        org.web3j.abi.datatypes.generated.Uint8.class,
                        org.web3j.abi.Utils.typeMap(serviceIDs, org.web3j.abi.datatypes.generated.Uint8.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint8>(
                        org.web3j.abi.datatypes.generated.Uint8.class,
                        org.web3j.abi.Utils.typeMap(prices, org.web3j.abi.datatypes.generated.Uint8.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Rendezvous load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Rendezvous(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Rendezvous load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Rendezvous(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Rendezvous load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Rendezvous(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Rendezvous load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Rendezvous(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Rendezvous> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Rendezvous.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Rendezvous> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Rendezvous.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Rendezvous> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Rendezvous.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Rendezvous> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Rendezvous.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
