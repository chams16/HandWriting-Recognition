package cs107KNN;
import java.nio.ByteBuffer;
public class KNN {
	public static void main(String[] args) {
		byte b1 = 40; // 00101000
		byte b2 = 20; // 00010100
		byte b3 = 10; // 00001010
		byte b4 = 5; // 00000101

		// [00101000 | 00010100 | 00001010 | 00000101] = 672401925
		int result = extractInt(b1, b2, b3, b4);
		System.out.println(result);

		String bits = "10000001";


		System.out.println("La séquence de bits " + bits + "\n\tinterprétée comme byte non signé donne "
				+ Helpers.interpretUnsigned(bits) + "\n\tinterpretée comme byte signé donne "
				+ Helpers.interpretSigned(bits));
				System.out.println("=== Test predictions ===");
		byte[][][] imagesTrain = KNN.parseIDXimages(Helpers.readBinaryFile("datasets/10-per-digit_images_train"));
		byte[] labelsTrain = KNN.parseIDXlabels(Helpers.readBinaryFile("datasets/10-per-digit_labels_train"));

		byte[][][] imagesTest = KNN.parseIDXimages(Helpers.readBinaryFile("datasets/10k_images_test"));
		byte[] labelsTest = KNN.parseIDXlabels(Helpers.readBinaryFile("datasets/10k_labels_test"));
		int test =700;
		int k=5;
		byte[] predictions = new byte[test];
		for (int i = 0; i < test; i++) {
			predictions[i] = KNN.knnClassify(imagesTest[i], imagesTrain, labelsTrain,k);
		}
		Helpers.show("Test predictions", imagesTest, predictions, labelsTest, 20, 35);
		System.out.println("Précision calculée: " + accuracy(predictions, labelsTest));
	}

	/**
	 * Composes four bytes into an integer using big endian convention.
	 *
	 * @param bXToBY The byte containing the bits to store between positions X and Y
	 * 
	 * @return the integer having form [ b31ToB24 | b23ToB16 | b15ToB8 | b7ToB0 ]
	 */
	public static int extractInt(byte b31ToB24, byte b23ToB16, byte b15ToB8, byte b7ToB0) {
		byte[] vect={b31ToB24, b23ToB16, b15ToB8, b7ToB0};
                ByteBuffer cont=ByteBuffer.wrap(vect);//le prog enveloppe le tableau de bits vect dans une case memoire appelée cont
                int rslt=cont.getInt();//lire l'entier stocké dans cont
		return rslt;
	}

	/**
	 * Parses an IDX file containing images
	 *
	 * @param data the binary content of the file
	 *
	 * @return A tensor of images
	 */
	public static byte[][][] parseIDXimages(byte[] data) {
		int nbimg = extractInt(data[4], data[5], data[6], data[7]);
		int hauteur =extractInt(data[8], data[9], data[10], data[11]);
		int largeur = extractInt(data[12], data[13], data[14], data[15]);
		int tailleimg = hauteur*largeur;
		byte [][][] tensor=new byte[nbimg][hauteur][largeur];
		int i=16;
		int numimg=0;
			while((i<nbimg*tailleimg)&&(numimg<nbimg)){
				tensor[numimg]= Extractoneimage(i, data,hauteur,largeur);
				i= i +tailleimg;
				numimg= numimg+1;
			}

		return tensor;
	}
	/**
	 * Extract one image
	 *
	 * @param data the binary content of the file
	 * @param hauteur,largeur width and height of one image
	 * @param countimg starting index of the image
	 * 
	 * @return A tensor of images
	 */
	public static byte[][] Extractoneimage(int countimg,byte[] data,int hauteur,int largeur){
		byte[][] image =new byte[hauteur][largeur];
		for(int h=0; h<hauteur;h++){
			for(int l=0; l<largeur;l++){
				image[h][l]=(byte) ((data[countimg] & 0xff)-128);
				countimg++;
			}

		}
		return image;
		
	}

	/**
	 * Parses an idx images containing labels
	 *
	 * @param data the binary content of the file
	 *
	 * @return the parsed labels
	 */
	public static byte[] parseIDXlabels(byte[] data) {
		int taille = extractInt(data[4], data[5], data[6], data[7]);
		byte[] labels = new byte[taille];
		for(int i=8;i<taille+8;i++){
			labels[i-8]= data[i];		//collecting labels starts from the 8th index
		}
		return labels;
	}

	/**
	 * @brief Computes the squared L2 distance of two images
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the squared euclidean distance between the two images
	 */
	public static float squaredEuclideanDistance(byte[][] a, byte[][] b) {
		float x =0;
		

		for (int i=0; i<a.length;i++){
			for(int j=0;j<a[0].length;j++){
				x+=Math.pow(a[i][j]-b[i][j],2);
				
			}

		}
		
        return x;
	}

	/**
	 * @brief Computes the inverted similarity between 2 images.
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the inverted similarity between the two images
	 */
	public static float invertedSimilarity(byte[][] a, byte[][] b) {
		float hauteur = a.length;
		float largeur = a[0].length;
		float moya =moyenne(a);
		float moyb =moyenne(b);
		float den = (float)Math.sqrt((double)methode(a)*(double)methode(b));
		float num =0;
		if(den==0){
			return 2;
		}
		else{
			for(int i=0;i<hauteur;i++){
				for(int j=0;j<largeur;j++){
				num += (a[i][j]-moyenne(a)) *(b[i][j]-moyenne(b));
				}
			}		
			return (1-(num/den));
		}
	}
	public static float moyenne(byte[][] img){
		float moy =0;
		float hauteur = img.length;
		float largeur = img[0].length;
		for(int k=0;k<hauteur;k++){
			for(int j=0;j<largeur;j++){
				moy += img[k][j];
			}
		}
		moy = moy/(hauteur*largeur);
		return moy;
	}
	public static float methode(byte[][] a) {
		double hauteur = a.length;
		double largeur = a[0].length;
		float oper = 0;
		float moy =moyenne(a);
		for(int i=0;i<hauteur;i++){
			for(int j=0;j<largeur;j++){
				oper += Math.pow((a[i][j]-moy),2);
			}
		}
		return oper;
	}
	/**
	 * @brief Quicksorts and returns the new indices of each value.
	 * 
	 * @param values the values whose indices have to be sorted in non decreasing
	 *               order
	 * 
	 * @return the array of sorted indices
	 * 
	 *         Example: values = quicksortIndices([3, 7, 0, 9]) gives [2, 0, 1, 3]
	 */
	public static int[] quicksortIndices(float[] values) {
		int [] indices = new int[values.length];
		for(int i=0; i<values.length;i++){
			indices[i]= i;
		}

		KNN.quicksortIndices(values, indices, 0, values.length-1);
		return(indices);
		
	}

	/**
	 * @brief Sorts the provided values between two indices while applying the same
	 *        transformations to the array of indices
	 * 
	 * @param values  the values to sort
	 * @param indices the indices to sort according to the corresponding values
	 * @param         low, high are the **inclusive** bounds of the portion of array
	 *                to sort
	 */
	public static void quicksortIndices(float[] values, int[] indices, int low, int high) {
		int l=low; 								
		int h= high;	
		float pivot = values[low]; 
		while(l <= h){
			if(values[l]<pivot){    
				l ++;				
			}
			else if(values[h] > pivot){
				h --;
			}
			else{
				swap(l, h, values, indices);	
				l ++;			
				h --;			
			}
		}
		if(low<h){
			quicksortIndices(values, indices, low, h);
		}
		if(high >l){    
			quicksortIndices(values, indices, l, high);
		}
	}

	/**
	 * @brief Swaps the elements of the given arrays at the provided positions
	 * 
	 * @param         i, j the indices of the elements to swap
	 * @param values  the array floats whose values are to be swapped
	 * @param indices the array of ints whose values are to be swapped
	 */
	public static void swap(int i, int j, float[] values, int[] indices) {
		float aux = values[j];
		values[j]= values[i];
		values[i]= aux;
		int aux2 = indices[j];
		indices[j] =indices[i];
		indices[i]=aux2;

	}

	/**
	 * @brief Returns the index of the largest element in the array
	 * 
	 * @param array an array of integers
	 * 
	 * @return the index of the largest integer
	 */
	public static int indexOfMax(int[] array) {
		int maxindex=0;
		int maxval= array[0];
		for(int i=0;i<array.length;i++){
			if(array[i]>maxval){
				maxval=array[i];
				maxindex=i;
			}
		}
		return maxindex;
	}

	/**
	 * The k first elements of the provided array vote for a label
	 *
	 * @param sortedIndices the indices sorted by non-decreasing distance
	 * @param labels        the labels corresponding to the indices
	 * @param k             the number of labels asked to vote
	 *
	 * @return the winner of the election
	 */
	public static byte electLabel(int[] sortedIndices, byte[] labels, int k) {
		int [] tab = new int [10];
		int [] tab2 = new int [10];
		for(int j=0;j<10;j++){
			tab2[j]= -1;
		}
		for(int i=0;i<k;i++){   
				if(tab2[labels[sortedIndices[i]]]== -1){
					tab2[labels[sortedIndices[i]]] = sortedIndices[i];
				}
				tab[labels[sortedIndices[i]]] ++;
		}
		int max =KNN.indexOfMax(tab); 
		int index = tab2[max]; 
		int max2=tab[max];
		tab[max]=0;
		while(max2==tab[KNN.indexOfMax(tab)]){
			if(tab2[KNN.indexOfMax(tab)]<index){
				max=KNN.indexOfMax(tab);
				index =tab2[max];
				tab[max]=0;

			}
			else{
				tab[KNN.indexOfMax(tab)]=0;
			}
		}
		
		return (byte)max;
	}

	/**
	 * Classifies the symbol drawn on the provided image
	 *
	 * @param image       the image to classify
	 * @param trainImages the tensor of training images
	 * @param trainLabels the list of labels corresponding to the training images
	 * @param k           the number of voters in the election process
	 *
	 * @return the label of the image
	 */
	public static byte knnClassify(byte[][] image, byte[][][] trainImages, byte[] trainLabels, int k) {
		float [] distance = new float [trainImages.length];
		for(int i=0; i<trainImages.length;i++){
			distance[i]= KNN.invertedSimilarity(image,trainImages[i]);
		}
		int [] indices =KNN.quicksortIndices(distance);
		return KNN.electLabel(indices, trainLabels ,k);
	}

	/**
	 * Computes accuracy between two arrays of predictions
	 * 
	 * @param predictedLabels the array of labels predicted by the algorithm
	 * @param trueLabels      the array of true labels
	 * 
	 * @return the accuracy of the predictions. Its value is in [0, 1]
	 */
	public static double accuracy(byte[] predictedLabels, byte[] trueLabels) {
		double somme =0;
		for(int i=0; i<predictedLabels.length;i++){
			if(predictedLabels[i]==trueLabels[i]){
				somme +=1;
			}
		}
		return somme/predictedLabels.length;
	}
}
