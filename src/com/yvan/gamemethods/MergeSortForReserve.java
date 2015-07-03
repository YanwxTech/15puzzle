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
			mergeSort(array, begin, mid, array2);// 左半部分递归调用
			mergeSort(array, mid + 1, end, array2);// 右半部分递归调用
			merge(array2, begin, mid, end, array1);// 由array2去归并，返回的值放到array1中,
			// array1赋新值，其实就是更新array2,然后让array2再去归并，返回新的array1
		}

	}

	/**
	 * 实现数组排序，并记录逆序个数
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
