package jp.osaka.cherry.work.util.helper;

import android.annotation.SuppressLint;
import android.content.Context;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jp.osaka.cherry.work.constants.CONTENT;
import jp.osaka.cherry.work.constants.PRIORITY;
import jp.osaka.cherry.work.constants.PROGRESS;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.File;

import static jp.osaka.cherry.work.constants.INVALID.INVALID_LONG_VALUE;
import static jp.osaka.cherry.work.constants.PRIORITY.toPRIORITY;
import static jp.osaka.cherry.work.constants.PROGRESS.toPROGRESS;
import static jp.osaka.cherry.work.constants.VALUE.toIntegerValue;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

/**
 * ファイルヘルパ
 */
public class FileHelper {

    /**
     * 一覧変換
     *
     * @param files 一覧
     * @param src 項目
     * @return 一覧
     */
    public static ArrayList<File> toListOf(ArrayList<File> files, String src) {
        ArrayList<File> result = new ArrayList<>();
        if (src.isEmpty()) {
            result.addAll(files);
            return result;
        }
        for (File item : files) {
            if (item != null && (item.name.contains(src))) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 検索一覧変換
     *
     * @param collection 一覧
     * @return 検索一覧
     */
    public static Collection<File> toSortByDateModifiedCollection(Collection<File> collection) {
        Collections.sort((List<File>) collection, (lhs, rhs) -> (int) (lhs.date - rhs.date));
        return collection;
    }

    /**
     * ファイル削除
     *
     * @param context コンテキスト
     * @param name 名前
     */
    public static void deleteFile(Context context, String name) {
        context.deleteFile(name + ".csv");
    }

    /**
     * CSV変換
     *
     * @param list 一覧　
     * @return CSV
     */
    public static String toCSV(ArrayList<Asset> list) {

        StringBuilder sb = new StringBuilder();
        for (Asset item : list) {
            sb.append("\"").append(item.id).append("\"").append(","); //id
            sb.append("\"").append(item.uuid).append("\"").append(","); //uuid
            sb.append("\"").append(item.creationDate).append("\"").append(","); //createDate
            sb.append("\"").append(item.modifiedDate).append("\"").append(","); //modifiedDate
            sb.append("\"").append(item.displayName).append("\"").append(","); //displayName
            sb.append("\"").append(item.startDate).append("\"").append(","); //startDate
            sb.append("\"").append(item.endDate).append("\"").append(","); //endDate
            sb.append("\"").append(item.progressState.name()).append("\"").append(","); //progressState
            sb.append("\"").append(item.priority.name()).append("\"").append(","); //priority.xml
            sb.append("\"").append(item.rate).append("\"").append(","); //rate
            sb.append("\"").append(item.note).append("\"").append(","); //note
            sb.append("\"").append(item.content.name()).append("\"").append(","); //content
            sb.append("\"").append(item.timestamp).append("\"").append("\n"); //timestamp
        }
        String data = sb.toString();
        sb.delete(0, sb.length());

        return data;
    }

    /**
     * ファイル読み込み
     *
     * @param context コンテキスト
     * @param file ファイル
     * @return 一覧
     */
    public static ArrayList<Asset> loadFile(Context context, String file) {
        ArrayList<Asset> result = new ArrayList<>();
        try {
            FileInputStream in = context.openFileInput(file);
            InputStreamReader ireader;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                ireader = new InputStreamReader(in, StandardCharsets.UTF_8);
            } else {
                ireader = new InputStreamReader(in);
            }
            CSVReader reader = new CSVReader(ireader, ',', '"', 0);
            List<String[]> records = reader.readAll();
            result = analyze(records);
            reader.close();
            ireader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 解析
     *
     * @param records 一覧
     * @return 一覧
     */
    private static ArrayList<Asset> analyze(List<String[]> records) {
        ArrayList<Asset> result = new ArrayList<>();

        for (String[] record : records) {
            if (record.length > 12) {
                try {
                    Asset task = new Asset(
                            record[0],
                            record[1],
                            toLong(record[2], System.currentTimeMillis()),
                            toLong(record[3], System.currentTimeMillis()),
                            record[4],
                            toLong(record[5], INVALID_LONG_VALUE),
                            toLong(record[6], INVALID_LONG_VALUE),
                            toPROGRESS(record[7], PROGRESS.NOT_START),
                            toPRIORITY(record[8], PRIORITY.MIDDLE),
                            toIntegerValue(record[9], 0),
                            record[10],
                            CONTENT.toCONTENT(record[11], CONTENT.INBOX),
                            toLong(record[12], INVALID_LONG_VALUE)
                    );
                    result.add(task);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                @SuppressLint("SimpleDateFormat") DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd");
                Asset task = Asset.createInstance();
                int i = 0;
                for (String r : record) {
                    switch (i) {
                        case 0: {
                            task.displayName = r;
                            break;
                        }
                        case 1: {
                            task.note = r;
                            break;
                        }
                        case 2: {
                            try {
                                Date date = dateTimeFormat.parse(r);
                                task.startDate = Objects.requireNonNull(date).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case 3: {
                            try {
                                Date date = dateTimeFormat.parse(r);
                                task.endDate = Objects.requireNonNull(date).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case 4: {
                            task.progressState = toPROGRESS(r, PROGRESS.NOT_START);
                            break;
                        }
                        case 5: {
                            task.priority = toPRIORITY(r, PRIORITY.MIDDLE);
                            break;
                        }
                        case 6: {
                            task.rate = toIntegerValue(r, 0);
                            break;
                        }
                        case 7: {
                            task.id = r;
                            break;
                        }
                        case 8: {
                            task.uuid = r;
                            break;
                        }
                        case 9: {
                            task.creationDate = toLong(r, System.currentTimeMillis());
                            break;
                        }
                        case 10: {
                            task.modifiedDate = toLong(r, System.currentTimeMillis());
                            break;
                        }
                        case 11: {
                            task.content = CONTENT.toCONTENT(r, CONTENT.INBOX);
                            break;
                        }
                        case 12: {
                            task.timestamp = toLong(r, System.currentTimeMillis());
                            break;
                        }
                        default:
                            break;
                    }
                    i++;
                }

                result.add(task);
            }
        }
        return result;
    }

    /**
     * ファイル保存
     *
     * @param context コンテキスト
     * @param filename ファイル名
     * @param list 一覧
     */
    public static void saveFile(Context context, String filename, ArrayList<Asset> list) {
        try {
            StringBuilder sb = new StringBuilder();
            FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);
            for (Asset item : list) {
                sb.append("\"").append(item.id).append("\"").append(","); //id
                sb.append("\"").append(item.uuid).append("\"").append(","); //uuid
                sb.append("\"").append(item.creationDate).append("\"").append(","); //createDate
                sb.append("\"").append(item.modifiedDate).append("\"").append(","); //modifiedDate
                sb.append("\"").append(item.displayName).append("\"").append(","); //displayName
                sb.append("\"").append(item.startDate).append("\"").append(","); //startDate
                sb.append("\"").append(item.endDate).append("\"").append(","); //endDate
                sb.append("\"").append(item.progressState.name()).append("\"").append(","); //progressState
                sb.append("\"").append(item.priority.name()).append("\"").append(","); //priority.xml
                sb.append("\"").append(item.rate).append("\"").append(","); //rate
                sb.append("\"").append(item.note).append("\"").append(","); //note
                sb.append("\"").append(item.content.name()).append("\"").append(","); //content
                sb.append("\"").append(item.timestamp).append("\"").append("\n"); //timestamp
            }
            String data = sb.toString();
            out.write(data.getBytes());
            out.flush();
            out.close();
            sb.delete(0, sb.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ファイル読み込み
     *
     * @param file ファイル
     * @return 一覧
     */
    public static ArrayList<Asset> loadFile(java.io.File file) {

        ArrayList<Asset> result = new ArrayList<>();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            CSVReader reader = new CSVReader(br, ',', '"', 0);
            List<String[]> records = reader.readAll();
            result = analyze(records);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
