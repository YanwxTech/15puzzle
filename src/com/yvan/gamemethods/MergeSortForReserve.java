package com.yvan.gamemethods;

public class MergeSortForReserve {
	public static int count = 0;

	public static int getReserve(int[] array, int begin, int end, int[] array1) {
		count = 0;
		mergeSort(array, begin, end, array1);
		return count;
	}

	public static void mergeSort(int[] array, int begin, int end, int[] array1) {
		int[] array2 = new int[end + 1];
		if (begin == end) {
			array1[begin] = array[begin];
		} else {
			int mid = (begin + end) / 2;
			mergeSort(array, begin, mid, array2);// ��벿�ֵݹ����
			mergeSort(array, mid + 1, end, array2);// �Ұ벿�ֵݹ����
			merge(array2, begin, mid, end, array1);// ��array2ȥ�鲢�����ص�ֵ�ŵ�array1��,
			// array1����ֵ����ʵ���Ǹ���array2,Ȼ����array2��ȥ�鲢�������µ�array1
		}

	}

	/**
	 * ʵ���������򣬲���¼�������
	 * 
	 * @param array2
	 * @param begin
	 * @param mid
	 * @param end
	 * @param array1
	 */
	private static void merge(int[] array2, int begin, int mid, int end,
			int[] array1) {

		int i = begin;
		int j = mid + 1;
		int k = begin;
		while (i <= mid && j <= end) {
			if (array2[i] < array2[j]) {
				array1[k++] = array2[i++];
			} else {
				array1[k++] = array2[j++];
				count += mid - i + 1;
			}
		}
		while (i <= mid) {
			array1[k++] = array2[i++];
		}
		while (j <= end) {
			array1[k++] = array2[j++];
		}

	}
}
