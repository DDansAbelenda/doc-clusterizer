## DocClusterizer

DocClusterizer is a Java desktop application designed to analyze and cluster documents based on their content similarity. The application utilizes Lucene and Tika libraries to process various file extensions such as txt, pdf, docx, and pptx. By extracting tokens and applying filtering techniques to remove stopwords in both English and Spanish, DocClusterizer creates a structured listing of documents and their associated tokens.

### Features

- **Document Processing:** DocClusterizer processes documents in different formats, extracting relevant tokens for further analysis.
- **Token Filtering:** Stopwords in English and Spanish are filtered out from the tokens to improve clustering accuracy.
- **Unsupervised Clustering Algorithms:** The application implements advanced unsupervised clustering algorithms including KMeans, FuzzyCMeans, and Linkage.
- **Intuitive Interface:** With a simple and intuitive interface, users can easily load documents from a selected directory and apply clustering algorithms with a single click.

### Installation

1. **Prerequisites:** Ensure you have Java 8 installed on your system.
2. **Download:** Clone this repository to your local machine.
   ```
   git clone https://github.com/DDansAbelenda/doc-clusterizer.git
   
   ```
3. **Library Dependencies:** The required libraries are included in the `lib` folder within the project directory.
4. **Compile:** Compile the Java source files using your preferred IDE or command-line compiler.
5. **Run:** Execute the compiled application to launch DocClusterizer.

### Usage

1. **Load Documents:** Select a directory containing the documents you want to analyze and cluster.
2. **Apply Clustering:** Click on the buttons corresponding to the clustering algorithms (KMeans, FuzzyCMeans, Linkage) to initiate the clustering process.
3. **View Results:** The application will display the clustered documents, providing insights into their semantic relationships.

### Contributing

Contributions are welcome! If you encounter any issues or have suggestions for improvements, please feel free to open an issue or submit a pull request.


### Disclaimer

This application is provided as-is, without any warranty or guarantee of its effectiveness for any specific purpose. Users are advised to use it responsibly and exercise caution when analyzing sensitive documents.

---
© 2024 DocClusterizer. Developed by [Daniel Dans Abelenda](https://github.com/DDansAbelenda).